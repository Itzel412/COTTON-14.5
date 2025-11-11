<script setup>
import { ref, computed, onMounted } from 'vue';
import { getFacturas, createFactura, getPedidos } from '../data/api';

const props = defineProps({
  currentUser: {
    type: Object,
    required: true,
  },
});

const esAdmin = computed(() => props.currentUser?.rol === 'ADMIN');

// Datos
const facturas = ref([]);
const pedidos = ref([]);

const loadingFacturas = ref(false);
const loadingPedidos = ref(false);

const error = ref(null);
const mensaje = ref(null);

// Para ADMIN: modo ver/crear y pedido seleccionado
const modoAdmin = ref('ver'); // 'ver' | 'crear'
const pedidoSeleccionado = ref(null);

const cerrarAlertas = () => {
  error.value = null;
  mensaje.value = null;
};

// ----------- CARGAS -----------

const cargarFacturas = async () => {
  loadingFacturas.value = true;
  error.value = null;
  try {
    facturas.value = await getFacturas();
  } catch (e) {
    error.value = e.message || 'Error al cargar facturas.';
  } finally {
    loadingFacturas.value = false;
  }
};

const cargarPedidos = async () => {
  if (!esAdmin.value) return;
  loadingPedidos.value = true;
  error.value = null;
  try {
    pedidos.value = await getPedidos();
  } catch (e) {
    error.value = e.message || 'Error al cargar pedidos.';
  } finally {
    loadingPedidos.value = false;
  }
};

const facturasFiltradas = computed(() => {
  if (esAdmin.value) {
    return facturas.value;
  }
  const correo = props.currentUser?.correo || '';
  return facturas.value.filter((f) => f.clienteCorreo === correo);
});

// ----------- ADMIN: CREAR FACTURA -----------

const seleccionarPedido = (pedido) => {
  cerrarAlertas();
  pedidoSeleccionado.value = pedido;
};

const confirmarFactura = async () => {
  if (!pedidoSeleccionado.value) return;

  cerrarAlertas();

  try {
    const ok = await createFactura(pedidoSeleccionado.value.id);
    if (!ok) {
      error.value = 'El backend no pudo registrar la factura.';
      return;
    }

    mensaje.value = 'Factura creada satisfactoriamente.';
    pedidoSeleccionado.value = null;
    modoAdmin.value = 'ver';

    await Promise.all([cargarFacturas(), cargarPedidos()]);
  } catch (e) {
    error.value = e.message || 'Error al registrar la factura.';
  }
};

const cancelarSeleccion = () => {
  pedidoSeleccionado.value = null;
};

// ----------- MOUNT -----------

onMounted(() => {
  cargarFacturas();
  cargarPedidos();
});
</script>

<template>
  <section class="facturas-wrapper">
    <div class="facturas-card">
      <header class="facturas-header">
        <h2 v-if="esAdmin">Gestión de facturas</h2>
        <h2 v-else>Mis facturas</h2>

        <p v-if="esAdmin">
          Crea nuevas facturas a partir de pedidos y consulta el historial completo.
        </p>
        <p v-else>
          Consulta las facturas generadas a partir de tus pedidos.
        </p>
      </header>

      <!-- Mensajes tipo modal -->
      <transition name="fade">
        <div v-if="error || mensaje" class="alert-overlay">
          <div
            class="alert-box"
            :class="{ 'alert-error': error, 'alert-success': mensaje }"
          >
            <p class="alert-text">
              {{ error || mensaje }}
            </p>
            <button type="button" class="alert-btn" @click="cerrarAlertas">
              OK
            </button>
          </div>
        </div>
      </transition>

      <!-- ADMIN -->
      <template v-if="esAdmin">
        <div class="fac-tabs">
          <button
            type="button"
            class="fac-tab-btn"
            :class="{ active: modoAdmin === 'ver' }"
            @click="modoAdmin = 'ver'"
          >
            Ver facturas
          </button>
          <button
            type="button"
            class="fac-tab-btn"
            :class="{ active: modoAdmin === 'crear' }"
            @click="modoAdmin = 'crear'"
          >
            Crear factura
          </button>
        </div>

        <!-- Panel VER FACTURAS -->
        <div v-if="modoAdmin === 'ver'" class="fac-panel">
          <h3 class="panel-title">Facturas registradas</h3>

          <p v-if="loadingFacturas">Cargando facturas...</p>

          <table v-else class="fac-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Pedido</th>
                <th>Cliente</th>
                <th>Color</th>
                <th>Talla</th>
                <th>Cantidad</th>
                <th>Subtotal</th>
                <th>IVA</th>
                <th>Total</th>
                <th>Fecha emisión</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="f in facturasFiltradas" :key="f.id">
                <td>{{ f.id }}</td>
                <td>#{{ f.idPedido }}</td>
                <td>{{ f.clienteCorreo }}</td>
                <td>{{ f.color }}</td>
                <td>{{ f.talla }}</td>
                <td>{{ f.cantidad }}</td>
                <td>{{ Number(f.subtotal).toFixed(2) }} $</td>
                <td>{{ Number(f.iva).toFixed(2) }} $</td>
                <td>{{ Number(f.total).toFixed(2) }} $</td>
                <td>{{ f.fechaEmision }}</td>
              </tr>
            </tbody>
          </table>

          <p v-if="!loadingFacturas && !facturasFiltradas.length">
            No hay facturas registradas.
          </p>
        </div>

        <!-- Panel CREAR FACTURA -->
        <div v-else class="fac-panel">
          <h3 class="panel-title">Crear factura desde pedido</h3>

          <p class="fac-panel-text">
            Selecciona un pedido realizado por un cliente para generar la
            factura correspondiente.
          </p>

          <p v-if="loadingPedidos">Cargando pedidos...</p>

          <table v-else class="fac-table">
            <thead>
              <tr>
                <th>ID pedido</th>
                <th>Cliente</th>
                <th>Color</th>
                <th>Talla</th>
                <th>Cantidad</th>
                <th>Precio unitario</th>
                <th>Total pedido</th>
                <th>Fecha</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in pedidos" :key="p.id">
                <td>{{ p.id }}</td>
                <td>{{ p.usuario }}</td>
                <td>{{ p.color }}</td>
                <td>{{ p.talla }}</td>
                <td>{{ p.cantidad }}</td>
                <td>{{ Number(p.precioUnitario).toFixed(2) }} $</td>
                <td>{{ Number(p.total).toFixed(2) }} $</td>
                <td>{{ p.fecha }}</td>
                <td>
                  <button
                    type="button"
                    class="btn-ambos"
                    style="padding-inline: 0.9rem;"
                    @click="seleccionarPedido(p)"
                  >
                    Seleccionar
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <p v-if="!loadingPedidos && !pedidos.length">
            No hay pedidos disponibles para facturar.
          </p>

          <div
            v-if="pedidoSeleccionado"
            class="fac-resumen-factura"
          >
            <h4>Resumen del pedido seleccionado</h4>
            <p>
              <strong>Pedido:</strong> #{{ pedidoSeleccionado.id }}
            </p>
            <p>
              <strong>Cliente:</strong> {{ pedidoSeleccionado.usuario }}
            </p>
            <p>
              <strong>Franela:</strong>
              {{ pedidoSeleccionado.color }} - Talla {{ pedidoSeleccionado.talla }}
            </p>
            <p>
              <strong>Cantidad:</strong> {{ pedidoSeleccionado.cantidad }}
            </p>
            <p>
              <strong>Precio unitario:</strong>
              {{ Number(pedidoSeleccionado.precioUnitario).toFixed(2) }} $
            </p>
            <p>
              <strong>Total pedido:</strong>
              {{ Number(pedidoSeleccionado.total).toFixed(2) }} $
            </p>

            <div class="fac-resumen-botones">
              <button
                type="button"
                class="btn-ambos"
                @click="confirmarFactura"
              >
                Confirmar factura
              </button>
              <button
                type="button"
                class="btn-ambos cancelar"
                @click="cancelarSeleccion"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      </template>

      <!-- CLIENTE: solo ver facturas propias -->
      <template v-else>
        <div class="fac-panel">
          <h3 class="panel-title">Mis facturas</h3>

          <p v-if="loadingFacturas">Cargando facturas...</p>

          <table v-else class="fac-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Pedido</th>
                <th>Color</th>
                <th>Talla</th>
                <th>Cantidad</th>
                <th>Subtotal</th>
                <th>IVA</th>
                <th>Total</th>
                <th>Fecha emisión</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="f in facturasFiltradas" :key="f.id">
                <td>{{ f.id }}</td>
                <td>#{{ f.idPedido }}</td>
                <td>{{ f.color }}</td>
                <td>{{ f.talla }}</td>
                <td>{{ f.cantidad }}</td>
                <td>{{ Number(f.subtotal).toFixed(2) }} $</td>
                <td>{{ Number(f.iva).toFixed(2) }} $</td>
                <td>{{ Number(f.total).toFixed(2) }} $</td>
                <td>{{ f.fechaEmision }}</td>
              </tr>
            </tbody>
          </table>

          <p v-if="!loadingFacturas && !facturasFiltradas.length">
            Todavía no tienes facturas registradas.
          </p>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.facturas-wrapper {
  padding: 2.5rem 1rem 3rem;
  display: flex;
  justify-content: center;
}

.facturas-card {
  background: var(--cotton-light, #fcf5e9);
  border-radius: 20px;
  padding: 2rem 1.75rem;
  max-width: 1100px;
  width: 100%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
}

.facturas-header {
  margin-bottom: 1.5rem;
}

.facturas-header h2 {
  font-size: 1.6rem;
  margin-bottom: 0.3rem;
  color: var(--cotton-dark, #1c262e);
}

.facturas-header p {
  color: #555;
  font-size: 0.95rem;
}

/* Tabs admin */

.fac-tabs {
  display: inline-flex;
  gap: 0.5rem;
  border-radius: 999px;
  background: #e6e6e6;
  padding: 0.2rem;
  margin-bottom: 1.5rem;
}

.fac-tab-btn {
  border: none;
  background: transparent;
  padding: 0.5rem 1rem;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  color: #555;
  transition: background 0.15s ease, color 0.15s ease;
}

.fac-tab-btn.active {
  background: #ffffff;
  color: var(--cotton-dark, #1c262e);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

/* Panel y tablas */

.fac-panel {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.5rem;
  border: 1px solid #dddddd;
}

.panel-title {
  font-size: 1.1rem;
  margin-bottom: 0.75rem;
  color: var(--cotton-dark, #1c262e);
}

.fac-panel-text {
  font-size: 0.9rem;
  color: #444;
  margin-bottom: 0.9rem;
}

.fac-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
}

.fac-table thead {
  background: #f0f0f0;
}

.fac-table th,
.fac-table td {
  padding: 0.55rem 0.6rem;
  border-bottom: 1px solid #e4e4e4;
  text-align: left;
}

.fac-table th {
  color: #1c262e;
  font-weight: 600;
}

.fac-table td {
  color: #333333;
}

/* Resumen factura */

.fac-resumen-factura {
  margin-top: 1.2rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e2e2;
  font-size: 0.9rem;
  color: #222;
}

.fac-resumen-factura h4 {
  font-size: 1rem;
  margin-bottom: 0.4rem;
}

.fac-resumen-factura p {
  margin-bottom: 0.2rem;
}

.fac-resumen-botones {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.8rem;
}

.fac-resumen-botones .cancelar {
  background: #6c757d;
  box-shadow: none;
}

/* Modal de mensajes */

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.alert-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.alert-box {
  background: #ffffff;
  padding: 1.4rem 1.6rem;
  border-radius: 16px;
  max-width: 420px;
  width: 90%;
  box-shadow: 0 18px 45px rgba(0, 0, 0, 0.3);
  text-align: center;
}

.alert-text {
  margin-bottom: 1rem;
  font-size: 0.95rem;
  color: #111827;
}

.alert-success .alert-text {
  color: #14532d;
}

.alert-error .alert-text {
  color: #7f1d1d;
}

.alert-btn {
  border: none;
  border-radius: 999px;
  padding: 0.55rem 1.4rem;
  background: var(--cotton-dark, #1b4965);
  color: #ffffff;
  font-weight: 600;
  cursor: pointer;
}
</style>
