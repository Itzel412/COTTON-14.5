<script setup>
import { ref, computed, onMounted } from 'vue';
import { getFacturas, createFactura, getPedidos, deleteFactura, updateFacturaEstado } from '../data/api';

const props = defineProps({ currentUser: { type: Object, required: true } });
const esAdmin = computed(() => props.currentUser?.rol === 'ADMIN');

// Datos
const facturas = ref([]);
const pedidos = ref([]);
const loading = ref(false);

// UI
const modoAdmin = ref('ver'); 
const grupoSeleccionado = ref(null);
const ESTADOS = ['PENDIENTE', 'PAGADA', 'ANULADA'];

// --- MODAL ACTUALIZADO PARA SOPORTAR CANCELAR ---
const modal = ref({ 
  visible: false, 
  tipo: 'info', 
  titulo: '', 
  mensaje: '', 
  accion: null,
  accionCancelar: null // Nuevo: Para revertir si se cancela
});

const mostrarAlerta = (t, m, k='info') => { 
  modal.value = { visible: true, tipo: k, titulo: t, mensaje: m, accion: null, accionCancelar: null }; 
};

// Modificado para recibir callback de cancelación
const mostrarConfirmacion = (t, m, cbAceptar, cbCancelar = null) => { 
  modal.value = { 
    visible: true, 
    tipo: 'confirm', 
    titulo: t, 
    mensaje: m, 
    accion: cbAceptar,
    accionCancelar: cbCancelar 
  }; 
};

const cerrarModal = () => { 
  modal.value.visible = false; 
};

const ejecutarAccionModal = () => { 
  if(modal.value.accion) modal.value.accion(); 
  cerrarModal(); 
};

const ejecutarCancelarModal = () => {
  // Si hay una acción definida para cancelar (como revertir el estado), la ejecutamos
  if(modal.value.accionCancelar) modal.value.accionCancelar();
  cerrarModal();
};

// --- CARGA ---
const cargarDatos = async () => {
  loading.value = true;
  try {
    const [rf, rp] = await Promise.all([ getFacturas(), esAdmin.value ? getPedidos() : Promise.resolve([]) ]);
    facturas.value = Array.isArray(rf) ? rf : [];
    pedidos.value = Array.isArray(rp) ? rp : [];
  } catch (e) {
    mostrarAlerta('Error', 'No se cargaron los datos.', 'error');
  } finally { loading.value = false; }
};

// --- FILTROS ---
const facturasFiltradas = computed(() => {
  if (esAdmin.value) return facturas.value;
  return facturas.value.filter(f => f.clienteCorreo === props.currentUser.correo);
});

const gruposPendientes = computed(() => {
  if (!esAdmin.value) return [];
  const codigosFacturados = new Set(facturas.value.map(f => f.codigoPedido));
  const grupos = {};
  pedidos.value.forEach(p => {
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
    grupos[p.codigo].totalGrupo += p.total;
  });
  return Object.values(grupos);
});

// --- ACCIONES DE ESTADO (CON CONFIRMACIÓN) ---

// 1. Esta función se llama al cambiar el Select
const solicitarCambioEstado = (factura) => {
  mostrarConfirmacion(
    'Cambiar Estado', 
    `¿Estás seguro de cambiar el estado a "${factura.estado}"?`, 
    () => confirmarCambioEstadoReal(factura), // Si acepta
    () => cargarDatos() // Si cancela: Recargamos para revertir el cambio visual del select
  );
};

// 2. Esta función llama a la API si el usuario aceptó
const confirmarCambioEstadoReal = async (fac) => {
  try {
    const ok = await updateFacturaEstado(fac.id, fac.estado);
    if(ok) {
      console.log("Estado actualizado correctamente");
    } else {
      throw new Error();
    }
  } catch {
    mostrarAlerta('Error', 'Fallo al actualizar estado en el servidor.', 'error');
    cargarDatos(); // Revertir si falla
  }
};

const solicitarEliminar = (id) => mostrarConfirmacion('Eliminar', '¿Estás seguro de borrar esta factura?', () => eliminarLogica(id));

const eliminarLogica = async (id) => {
  if(await deleteFactura(id)) {
    facturas.value = facturas.value.filter(f => f.id !== id);
    mostrarAlerta('Eliminado', 'Factura borrada.', 'success');
  } else mostrarAlerta('Error', 'No se pudo borrar.', 'error');
};

// --- CREAR FACTURA ---
const seleccionarGrupo = (g) => { grupoSeleccionado.value = g; };
const cancelarSeleccion = () => { grupoSeleccionado.value = null; };

const confirmarCreacion = async () => {
  if (!grupoSeleccionado.value) return;
  
  // Enviamos el objeto con el ID como pide el backend corregido
  const payload = { id: grupoSeleccionado.value.idReferencia };

  try {
    const ok = await createFactura(payload); 
    if (ok) {
      mostrarAlerta('Éxito', 'Factura agrupada creada.', 'success');
      grupoSeleccionado.value = null;
      modoAdmin.value = 'ver';
      await cargarDatos();
    } else {
      mostrarAlerta('Error', 'El backend rechazó la creación.', 'error');
    }
  } catch (e) { mostrarAlerta('Error', 'Fallo de red.', 'error'); }
};

onMounted(cargarDatos);
</script>

<template>
  <section class="facturas-wrapper">
    <div class="main-card">
      <header class="header-card">
        <h2>{{ esAdmin ? 'Gestión de Facturación' : 'Mis Facturas' }}</h2>
      </header>

      <div v-if="esAdmin" class="tabs-nav">
        <button class="tab-btn" :class="{ active: modoAdmin === 'ver' }" @click="modoAdmin = 'ver'">
          Historial ({{ facturasFiltradas.length }})
        </button>
        <button class="tab-btn" :class="{ active: modoAdmin === 'crear' }" @click="modoAdmin = 'crear'">
          Por Facturar ({{ gruposPendientes.length }})
        </button>
      </div>

      <div v-if="modoAdmin === 'ver'" class="panel-content">
        <div v-if="loading" class="loading">Cargando...</div>
        <div v-else-if="!facturasFiltradas.length" class="vacio-msg">Sin facturas.</div>
        
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
              <td class="resaltado">{{ f.codigoPedido }}</td>
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
                <button class="btn-eliminar" @click="solicitarEliminar(f.id)">Borrar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="esAdmin && modoAdmin === 'crear'" class="panel-content">
        <h3>Órdenes Pendientes</h3>
        
        <div v-if="!grupoSeleccionado">
          <div v-if="!gruposPendientes.length" class="vacio-msg">Todo facturado.</div>
          <table v-else class="cotton-table">
            <thead>
              <tr><th>Código</th><th>Cliente</th><th>Items</th><th>Total</th><th>Acción</th></tr>
            </thead>
            <tbody>
              <tr v-for="g in gruposPendientes" :key="g.codigo">
                <td class="resaltado">{{ g.codigo }}</td>
                <td class="dato">{{ g.cliente }}</td>
                <td class="dato">{{ g.items.length }} productos</td>
                <td class="precio">${{ Number(g.totalGrupo).toFixed(2) }}</td>
                <td><button class="btn-small" @click="seleccionarGrupo(g)">Facturar Orden</button></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="confirmacion-box">
          <h4>Confirmar Facturación de Orden</h4>
          <div class="detalles-factura">
            <p><strong>Código:</strong> {{ grupoSeleccionado.codigo }}</p>
            <p><strong>Cliente:</strong> {{ grupoSeleccionado.cliente }}</p>
            <p><strong>Items:</strong> {{ grupoSeleccionado.items.length }}</p>
            <p><strong>Total Base:</strong> ${{ grupoSeleccionado.totalGrupo.toFixed(2) }}</p>
            <p class="nota-iva">* Se agregará el 16% de IVA al generar.</p>
          </div>
          <div class="botones-accion">
            <button class="btn-cancelar" @click="cancelarSeleccion">Volver</button>
            <button class="btn-confirmar" @click="confirmarCreacion">Generar Factura</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="modal.visible" class="modal-overlay">
      <div class="modal-box" :class="modal.tipo">
        <h3>{{ modal.titulo }}</h3>
        <p>{{ modal.mensaje }}</p>
        <div class="modal-btns">
          <button v-if="modal.tipo === 'confirm'" class="btn-modal cancel" @click="ejecutarCancelarModal">Cancelar</button>
          <button class="btn-modal ok" @click="ejecutarAccionModal">Aceptar</button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.facturas-wrapper { padding: 2rem; display: flex; justify-content: center; color: #333; }
.main-card { background: white; padding: 2rem; border-radius: 12px; width: 100%; max-width: 1100px; box-shadow: 0 5px 20px rgba(0,0,0,0.1); }
.cotton-table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
.cotton-table th { text-align: left; padding: 1rem; background: #f4f4f4; color: #1c262e; }
.cotton-table td { padding: 1rem; border-bottom: 1px solid #eee; vertical-align: middle; }
.dato { font-weight: 500; }
.desc { font-size: 0.9rem; color: #666; }
.resaltado { font-family: monospace; background: #eee; padding: 2px 6px; border-radius: 4px; font-weight: bold; }
.precio { font-weight: 700; color: #27ae60; }
.tabs-nav { display: flex; gap: 1rem; margin-bottom: 1rem; border-bottom: 1px solid #eee; padding-bottom: 1rem; }
.tab-btn { background: none; border: none; padding: 0.5rem 1rem; cursor: pointer; color: #888; font-weight: 600; }
.tab-btn.active { color: #1c262e; border-bottom: 2px solid #1c262e; }
.btn-eliminar { background: #dc3545; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; }
.btn-small { background: #1c262e; color: white; padding: 6px 12px; border-radius: 4px; cursor: pointer; border: none;}
.confirmacion-box { background: #f9f9f9; padding: 2rem; text-align: center; border-radius: 8px; }
.botones-accion { display: flex; gap: 1rem; justify-content: center; margin-top: 1rem; }
.btn-confirmar { background: #27ae60; color: white; padding: 10px 20px; border: none; border-radius: 6px; cursor: pointer; }
.btn-cancelar { background: #ccc; padding: 10px 20px; border: none; border-radius: 6px; cursor: pointer; }
.select-estado { padding: 5px; border-radius: 4px; border: 1px solid #ccc; cursor: pointer; font-weight: bold; }
/* Colores de estado */
.pendiente { background: #fff3cd; color: #856404; } 
.pagada { background: #d4edda; color: #155724; } 
.anulada { background: #f8d7da; color: #721c24; }
.select-estado.pendiente { background-color: #fff3cd; }
.select-estado.pagada { background-color: #d4edda; }
.select-estado.anulada { background-color: #f8d7da; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 999; }
.modal-box { background: white; padding: 2rem; border-radius: 8px; width: 350px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.2); }
.modal-btns { display: flex; gap: 10px; justify-content: center; margin-top: 1.5rem; }
.btn-modal { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
.btn-modal.cancel { background: #e0e0e0; color: #333; }
.btn-modal.ok { background: #1c262e; color: white; }
</style>