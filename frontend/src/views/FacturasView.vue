<script setup>
import { ref, computed, onMounted } from 'vue';
import { getFacturas, createFactura, getPedidos, deleteFactura, updateFacturaEstado } from '../data/api';

const props = defineProps({ currentUser: { type: Object, required: true } });
const esAdmin = computed(() => props.currentUser?.rol === 'ADMIN');

const facturas = ref([]);
const pedidos = ref([]);
const loading = ref(false);

const modoAdmin = ref('ver');
const grupoSeleccionado = ref(null);
const ESTADOS = ['PENDIENTE', 'PAGADA', 'ANULADA'];

const modal = ref({
  visible: false,
  tipo: 'info', 
  titulo: '',
  mensaje: '',
  accion: null,
  accionCancelar: null
});

const mostrarAlerta = (t, m, k='info') => {
  modal.value = { visible: true, tipo: k, titulo: t, mensaje: m, accion: null, accionCancelar: null };
};

const mostrarConfirmacion = (t, m, cbAceptar, cbCancelar = null) => {
  modal.value = { visible: true, tipo: 'confirm', titulo: t, mensaje: m, accion: cbAceptar, accionCancelar: cbCancelar };
};

const cerrarModal = () => { modal.value.visible = false; };
const ejecutarAccionModal = () => { if (modal.value.accion) modal.value.accion(); cerrarModal(); };
const ejecutarCancelarModal = () => { if (modal.value.accionCancelar) modal.value.accionCancelar(); cerrarModal(); };

const cargarDatos = async () => {
  loading.value = true;
  try {
    const [rf, rp] = await Promise.all([
      getFacturas(),
      esAdmin.value ? getPedidos() : Promise.resolve([])
    ]);
    facturas.value = Array.isArray(rf) ? rf : [];
    pedidos.value = Array.isArray(rp) ? rp : [];
  } catch (e) {
    mostrarAlerta('Error', 'No se cargaron los datos.', 'error');
  } finally {
    loading.value = false;
  }
};

const facturasFiltradas = computed(() => {
  if (esAdmin.value) return facturas.value;
  const correo = (props.currentUser?.correo || '').trim();
  return facturas.value.filter(f => (f.clienteCorreo || '').trim() === correo);
});

const gruposPendientes = computed(() => {
  if (!esAdmin.value) return [];
  const codigosFacturados = new Set(facturas.value.map(f => f.codigoPedido));
  const grupos = {};

  pedidos.value.forEach(p => {
    if (!p?.codigo) return;
    if (codigosFacturados.has(p.codigo)) return;

    if (!grupos[p.codigo]) {
      grupos[p.codigo] = {
        codigo: p.codigo,
        cliente: p.usuario,
        totalGrupo: 0,
        items: [],
        idReferencia: p.id
      };
    }
    grupos[p.codigo].items.push(p);
    grupos[p.codigo].totalGrupo += Number(p.total || 0);
  });

  return Object.values(grupos).sort((a,b) => (a.codigo < b.codigo ? 1 : -1));
});

const solicitarCambioEstado = (factura) => {
  mostrarConfirmacion(
    'Cambiar Estado',
    `¿Confirmas cambiar el estado a "${factura.estado}"?`,
    () => confirmarCambioEstadoReal(factura),
    () => cargarDatos()
  );
};

const confirmarCambioEstadoReal = async (fac) => {
  try {
    const ok = await updateFacturaEstado(fac.id, fac.estado);
    if (!ok) throw new Error();
  } catch {
    mostrarAlerta('Error', 'Fallo al actualizar estado en el servidor.', 'error');
    await cargarDatos();
  }
};

const solicitarEliminar = (id) =>
  mostrarConfirmacion('Eliminar factura', '¿Seguro que deseas borrar esta factura?', () => eliminarLogica(id));

const eliminarLogica = async (id) => {
  if (await deleteFactura(id)) {
    facturas.value = facturas.value.filter(f => f.id !== id);
    mostrarAlerta('Eliminado', 'Factura borrada.', 'success');
  } else {
    mostrarAlerta('Error', 'No se pudo borrar.', 'error');
  }
};

const seleccionarGrupo = (g) => { grupoSeleccionado.value = g; };
const cancelarSeleccion = () => { grupoSeleccionado.value = null; };

const confirmarCreacion = async () => {
  if (!grupoSeleccionado.value) return;

  const payload = { id: grupoSeleccionado.value.idReferencia };

  try {
    const ok = await createFactura(payload);
    if (ok) {
      mostrarAlerta('Éxito', 'Factura creada correctamente.', 'success');
      grupoSeleccionado.value = null;
      modoAdmin.value = 'ver';
      await cargarDatos();
    } else {
      mostrarAlerta('Rechazado', 'El backend rechazó la creación (posible ya facturado).', 'error');
    }
  } catch {
    mostrarAlerta('Error', 'Fallo de red.', 'error');
  }
};

onMounted(cargarDatos);
</script>

<template>
  <section class="facturas-wrapper">
    <div class="facturas-card">
      <header class="facturas-header">
        <h2>{{ esAdmin ? 'Gestión de Facturación' : 'Mis Facturas' }}</h2>
        <p class="sub">
          {{ esAdmin ? 'Crea facturas desde pedidos y gestiona estados.' : 'Consulta tus facturas generadas.' }}
        </p>
      </header>

      <div v-if="esAdmin" class="tabs-nav">
        <button class="tab-btn" :class="{ active: modoAdmin === 'ver' }" @click="modoAdmin = 'ver'">
          Historial ({{ facturasFiltradas.length }})
        </button>
        <button class="tab-btn" :class="{ active: modoAdmin === 'crear' }" @click="modoAdmin = 'crear'">
          Por facturar ({{ gruposPendientes.length }})
        </button>
      </div>

      <div v-if="modoAdmin === 'ver'" class="panel">
        <p v-if="loading">Cargando...</p>
        <p v-else-if="!facturasFiltradas.length" class="vacio">No hay facturas.</p>

        <table v-else class="cotton-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Código Pedido</th>
              <th>Cliente</th>
              <th>Detalle</th>
              <th>Total (+IVA)</th>
              <th>Estado</th>
              <th v-if="esAdmin">Acción</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in facturasFiltradas" :key="f.id">
              <td class="dato">#{{ f.id }}</td>
              <td><span class="code">{{ f.codigoPedido }}</span></td>
              <td class="dato">{{ f.clienteCorreo }}</td>
              <td class="desc">{{ f.descripcion }}</td>
              <td class="precio">${{ Number(f.total).toFixed(2) }}</td>

              <td>
                <div v-if="esAdmin">
                  <select
                    v-model="f.estado"
                    class="select-estado"
                    :class="f.estado ? f.estado.toLowerCase() : ''"
                    @change="solicitarCambioEstado(f)"
                  >
                    <option v-for="e in ESTADOS" :key="e" :value="e">{{ e }}</option>
                  </select>
                </div>
                <span v-else class="badge" :class="f.estado ? f.estado.toLowerCase() : ''">{{ f.estado }}</span>
              </td>

              <td v-if="esAdmin">
                <button class="btn-danger" @click="solicitarEliminar(f.id)">Borrar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="esAdmin && modoAdmin === 'crear'" class="panel">
        <h3 class="panel-title">Órdenes pendientes</h3>

        <div v-if="!grupoSeleccionado">
          <p v-if="!gruposPendientes.length" class="vacio">Todo está facturado.</p>

          <table v-else class="cotton-table">
            <thead>
              <tr><th>Código</th><th>Cliente</th><th>Items</th><th>Total</th><th>Acción</th></tr>
            </thead>
            <tbody>
              <tr v-for="g in gruposPendientes" :key="g.codigo">
                <td><span class="code">{{ g.codigo }}</span></td>
                <td class="dato">{{ g.cliente }}</td>
                <td class="dato">{{ g.items.length }} items</td>
                <td class="precio">${{ Number(g.totalGrupo).toFixed(2) }}</td>
                <td><button class="btn-primary" @click="seleccionarGrupo(g)">Facturar</button></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="confirm-box">
          <h4>Confirmar facturación</h4>
          <div class="confirm-body">
            <p><strong>Código:</strong> <span class="code">{{ grupoSeleccionado.codigo }}</span></p>
            <p><strong>Cliente:</strong> {{ grupoSeleccionado.cliente }}</p>
            <p><strong>Items:</strong> {{ grupoSeleccionado.items.length }}</p>
            <p><strong>Total base:</strong> ${{ grupoSeleccionado.totalGrupo.toFixed(2) }}</p>
            <p class="nota">Se agregará 16% de IVA al generar.</p>
          </div>

          <div class="confirm-actions">
            <button class="btn-secondary" @click="cancelarSeleccion">Volver</button>
            <button class="btn-primary" @click="confirmarCreacion">Generar factura</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="modal.visible" class="modal-backdrop">
      <div class="modal-card">
        <h3>{{ modal.titulo }}</h3>
        <p>{{ modal.mensaje }}</p>

        <div class="modal-actions">
          <button v-if="modal.tipo === 'confirm'" class="btn-secondary" @click="ejecutarCancelarModal">Cancelar</button>
          <button class="btn-primary" @click="ejecutarAccionModal">Aceptar</button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.facturas-wrapper{
  padding: 2.5rem 1rem 3rem;
  display:flex;
  justify-content:center;
}

.facturas-card{
  background: #fcf5e9;
  border-radius: 20px;
  padding: 2rem 1.75rem;
  max-width: 1200px;
  width: 100%;
  box-shadow: 0 10px 30px rgba(0,0,0,0.18);
  color:#1c262e;
}

.facturas-header{
  margin-bottom: 1.25rem;
}
.facturas-header h2{
  margin:0 0 .25rem;
  font-size:1.6rem;
}
.sub{
  margin:0;
  color:#555;
  font-size:.95rem;
}

.tabs-nav{
  display:flex;
  gap:1rem;
  justify-content:center;
  margin: 1.25rem 0 1.75rem;
}
.tab-btn{
  background: transparent;
  border: 2px solid rgba(28,38,46,.2);
  padding: .6rem 1.5rem;
  border-radius: 50px;
  color:#666;
  font-weight:700;
  cursor:pointer;
}
.tab-btn.active{
  background:#1c262e;
  border-color:#1c262e;
  color:#fff;
}

.panel{
  background:#fff;
  border-radius:16px;
  padding:1.5rem;
  border:1px solid #ddd;
}

.panel-title{
  margin:0 0 .75rem;
  font-size:1.1rem;
}

.vacio{
  color:#555;
  margin:0;
}

.cotton-table{
  width:100%;
  border-collapse: collapse;
  margin-top:1rem;
  font-size:.92rem;
  background:#fff;
  border-radius: 12px;
  overflow:hidden;
}
.cotton-table thead{
  background:#f0f0f0;
}
.cotton-table th, .cotton-table td{
  padding:.75rem .75rem;
  border-bottom:1px solid #e4e4e4;
  text-align:left;
  vertical-align: middle;
}
.dato{ 
  font-weight:600; 
  color:#1c262e; 
}
.desc{ 
  color:#555; 
  font-size:.9rem; 
}
.precio{ 
  font-weight:900; 
  color:#1c262e; 
}

.code{
  font-family: monospace;
  background:#eee;
  padding:2px 6px;
  border-radius:6px;
  font-weight:800;
  color:#333;
}

.select-estado{
  padding:6px 10px;
  border-radius:10px;
  border:1px solid #ccc;
  font-weight:900;
  cursor:pointer;
}

.badge{
  padding:6px 10px;
  border-radius:10px;
  font-weight:900;
  display:inline-block;
}

.pendiente{ 
  background:#fff3cd; 
  color:#856404; 
}
.pagada{ 
  background:#d4edda; 
  color:#155724; 
}
.anulada{ 
  background:#f8d7da; 
  color:#721c24; 
}

.select-estado.pendiente{ 
  background:#fff3cd; 
}
.select-estado.pagada{ 
  background:#d4edda; 
}
.select-estado.anulada{ 
  background:#f8d7da; 
}

.btn-primary{
  background:#1c262e;
  color:#fff;
  border:none;
  border-radius:10px;
  padding:.65rem 1rem;
  font-weight:900;
  cursor:pointer;
}
.btn-secondary{
  background:#f0f0f0;
  color:#333;
  border:none;
  border-radius:10px;
  padding:.65rem 1rem;
  font-weight:900;
  cursor:pointer;
}
.btn-danger{
  background:#e74c3c;
  color:#fff;
  border:none;
  border-radius:10px;
  padding:.55rem .9rem;
  font-weight:900;
  cursor:pointer;
}

.confirm-box{
  background:#fff;
  border:1px solid #eee;
  border-radius:16px;
  padding:1.5rem;
}
.confirm-body p{ margin:.35rem 0; color:#1c262e; }
.nota{ color:#666; font-size:.9rem; margin-top:.5rem; }
.confirm-actions{
  display:flex;
  gap:1rem;
  justify-content:flex-end;
  margin-top:1rem;
}

.modal-backdrop{
  position: fixed;
  inset:0;
  background: rgba(28,38,46,0.6);
  display:flex;
  justify-content:center;
  align-items:center;
  z-index:1000;
}
.modal-card{
  background:#fff;
  padding:1.6rem;
  border-radius:16px;
  width:min(420px, 92%);
  box-shadow: 0 15px 40px rgba(0,0,0,0.2);
}
.modal-actions{
  display:flex;
  gap:1rem;
  justify-content:flex-end;
  margin-top:1rem;
}
</style>
