<script setup>
import { ref, computed, onMounted } from 'vue';
import { getProductos, getPedidos, createPedido } from '../data/api';

const props = defineProps({
  currentUser: {
    type: Object,
    required: true,
  },
});

const esAdmin = computed(() => props.currentUser?.rol === 'ADMIN');

const productos = ref([]);
const pedidos = ref([]);

const loadingProductos = ref(false);
const loadingPedidos = ref(false);

const error = ref(null);
const mensaje = ref(null);

const modoCliente = ref('catalogo'); // 'catalogo' | 'mis-pedidos'

// Pedido en construcción
const productoSeleccionado = ref(null);
const pasoPedido = ref('form'); // 'form' | 'confirm'
const pedidoDraft = ref({
  cantidad: 1,
});

const MAX_CANTIDAD = 10;

// --------- CARGA DE DATOS ---------
const cargarProductos = async () => {
  if (esAdmin.value) return; // admin no necesita catálogo
  loadingProductos.value = true;
  error.value = null;
  try {
    productos.value = await getProductos();
  } catch (e) {
    error.value = e.message || 'Error al cargar productos.';
  } finally {
    loadingProductos.value = false;
  }
};

const cargarPedidos = async () => {
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

// Pedidos filtrados por rol
const pedidosFiltrados = computed(() => {
  if (esAdmin.value) {
    return pedidos.value;
  }
  const correo = props.currentUser?.correo || '';
  return pedidos.value.filter((p) => p.usuario === correo);
});

// --------- FLUJO CLIENTE: NUEVO PEDIDO ---------
const iniciarPedido = (producto) => {
  mensaje.value = null;
  error.value = null;
  productoSeleccionado.value = producto;
  pedidoDraft.value = { cantidad: 1 };
  pasoPedido.value = 'form';
};

const prepararConfirmacionPedido = () => {
  if (!productoSeleccionado.value) return;

  error.value = null;
  mensaje.value = null;

  const cantidad = Number(pedidoDraft.value.cantidad || 0);
  const stock = Number(productoSeleccionado.value.stock || 0);

  const errores = [];
  if (cantidad <= 0) errores.push('La cantidad debe ser mayor a 0');
  if (cantidad > MAX_CANTIDAD)
    errores.push(`No se permiten más de ${MAX_CANTIDAD} unidades por pedido`);
  if (cantidad > stock)
    errores.push('La cantidad supera el stock disponible');

  if (errores.length) {
    error.value = errores.join('. ');
    return;
  }

  pasoPedido.value = 'confirm';
};

const confirmarPedido = async () => {
  if (!productoSeleccionado.value) return;

  error.value = null;

  const prod = productoSeleccionado.value;
  const cantidad = Number(pedidoDraft.value.cantidad || 0);

  const payload = {
    usuario: props.currentUser.correo,
    idProducto: prod.id,
    color: prod.color,
    talla: prod.talla,
    cantidad,
    fecha: new Date().toISOString().split('T')[0],
  };

  try {
    const ok = await createPedido(payload);
    if (!ok) {
      error.value = 'El backend no pudo registrar el pedido.';
      return;
    }

    mensaje.value = 'Pedido registrado satisfactoriamente.';

    // reset
    productoSeleccionado.value = null;
    pasoPedido.value = 'form';
    pedidoDraft.value = { cantidad: 1 };

    // refrescar datos
    await Promise.all([cargarPedidos(), cargarProductos()]);
  } catch (e) {
    error.value = e.message || 'Error al registrar el pedido.';
  }
};

const cancelarPedido = () => {
  productoSeleccionado.value = null;
  pedidoDraft.value = { cantidad: 1 };
  pasoPedido.value = 'form';
};

// --------- INIT ---------
onMounted(() => {
  if (!esAdmin.value) {
    cargarProductos();
  }
  cargarPedidos();
});
</script>

<template>
  <section class="pedidos-wrapper">
    <div class="pedidos-card">
      <header class="pedidos-header">
        <h2 v-if="esAdmin">Gestión de pedidos</h2>
        <h2 v-else>Pedidos y catálogo</h2>

        <p v-if="esAdmin">
          Consulta los pedidos realizados por los clientes.
        </p>
        <p v-else>
          Realiza pedidos desde el catálogo y revisa el historial de tus compras.
        </p>
      </header>

      <!-- MODAL DE MENSAJES -->
      <transition name="fade">
        <div v-if="error || mensaje" class="alert-overlay">
          <div
            class="alert-box"
            :class="{ 'alert-error': error, 'alert-success': mensaje }"
          >
            <p class="alert-text">
              {{ error || mensaje }}
            </p>
            <button
              type="button"
              class="alert-btn"
              @click="() => { error = null; mensaje = null; }"
            >
              OK
            </button>
          </div>
        </div>
      </transition>

      <!-- VISTA ADMIN -->
      <template v-if="esAdmin">
        <div class="ped-panel">
          <h3 class="panel-title">Pedidos registrados</h3>

          <p v-if="loadingPedidos">Cargando pedidos...</p>

          <table v-else class="ped-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Cliente</th>
                <th>Color</th>
                <th>Talla</th>
                <th>Cantidad</th>
                <th>Precio unitario</th>
                <th>Total</th>
                <th>Fecha</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in pedidosFiltrados" :key="p.id">
                <td>{{ p.id }}</td>
                <td>{{ p.usuario }}</td>
                <td>{{ p.color }}</td>
                <td>{{ p.talla }}</td>
                <td>{{ p.cantidad }}</td>
                <td>{{ Number(p.precioUnitario).toFixed(2) }} $</td>
                <td>{{ Number(p.total).toFixed(2) }} $</td>
                <td>{{ p.fecha }}</td>
              </tr>
            </tbody>
          </table>

          <p v-if="!loadingPedidos && !pedidosFiltrados.length">
            No hay pedidos registrados.
          </p>
        </div>
      </template>

      <!-- VISTA CLIENTE -->
      <template v-else>
        <div class="ped-tabs">
          <button
            type="button"
            class="ped-tab-btn"
            :class="{ active: modoCliente === 'catalogo' }"
            @click="modoCliente = 'catalogo'"
          >
            Ver catálogo
          </button>
          <button
            type="button"
            class="ped-tab-btn"
            :class="{ active: modoCliente === 'mis-pedidos' }"
            @click="modoCliente = 'mis-pedidos'"
          >
            Mis pedidos
          </button>
        </div>

        <!-- Catálogo -->
        <div v-if="modoCliente === 'catalogo'" class="ped-panel">
          <h3 class="panel-title">Catálogo de franelas</h3>

          <p v-if="loadingProductos">Cargando productos...</p>

          <div v-else class="catalog-grid">
            <article
              v-for="prod in productos"
              :key="prod.id"
              class="prod-card"
            >
              <h4>{{ prod.color }} · Talla {{ prod.talla }}</h4>
              <p class="prod-price">{{ Number(prod.precio).toFixed(2) }} $</p>
              <p class="prod-stock">Stock disponible: {{ prod.stock }}</p>
              <button
                type="button"
                class="btn-ambos prod-btn"
                :disabled="prod.stock === 0"
                @click="iniciarPedido(prod)"
              >
                {{ prod.stock === 0 ? 'Sin stock' : 'Hacer pedido' }}
              </button>
            </article>
          </div>

          <p v-if="!loadingProductos && !productos.length">
            No hay productos cargados en el catálogo.
          </p>

          <!-- Bloque NUEVO PEDIDO -->
          <div
            v-if="productoSeleccionado"
            class="nuevo-pedido-panel"
          >
            <h3>Nuevo pedido</h3>

            <p class="nuevo-pedido-resumen">
              Franela <strong>{{ productoSeleccionado.color }}</strong>,
              talla <strong>{{ productoSeleccionado.talla }}</strong>.
              Precio: {{ Number(productoSeleccionado.precio).toFixed(2) }} $.
              Stock disponible: {{ productoSeleccionado.stock }} unidades.
            </p>

            <!-- Paso 1: cantidad -->
            <div v-if="pasoPedido === 'form'">
              <div class="form-group">
                <label>Cantidad a pedir (máx. {{ MAX_CANTIDAD }})</label>
                <input
                  type="number"
                  min="1"
                  :max="MAX_CANTIDAD"
                  v-model.number="pedidoDraft.cantidad"
                />
              </div>

              <div class="nuevo-pedido-botones">
                <button
                  type="button"
                  class="btn-ambos"
                  @click="prepararConfirmacionPedido"
                >
                  Validar y continuar
                </button>
                <button
                  type="button"
                  class="btn-ambos cancelar"
                  @click="cancelarPedido"
                >
                  Cancelar
                </button>
              </div>
            </div>

            <!-- Paso 2: confirmación -->
            <div v-else>
              <p>
                Vas a pedir
                <strong>{{ pedidoDraft.cantidad }}</strong>
                unidad(es) de esta franela.
              </p>
              <p>
                Total estimado:
                <strong>
                  {{
                    (
                      Number(productoSeleccionado.precio) *
                      Number(pedidoDraft.cantidad)
                    ).toFixed(2)
                  }}
                  $
                </strong>
              </p>

              <div class="nuevo-pedido-botones">
                <button
                  type="button"
                  class="btn-ambos"
                  @click="confirmarPedido"
                >
                  Confirmar pedido
                </button>
                <button
                  type="button"
                  class="btn-ambos cancelar"
                  @click="cancelarPedido"
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Mis pedidos -->
        <div v-else class="ped-panel">
          <h3 class="panel-title">Mis pedidos</h3>

          <p v-if="loadingPedidos">Cargando pedidos...</p>

          <table v-else class="ped-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Color</th>
                <th>Talla</th>
                <th>Cantidad</th>
                <th>Precio unitario</th>
                <th>Total</th>
                <th>Fecha</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in pedidosFiltrados" :key="p.id">
                <td>{{ p.id }}</td>
                <td>{{ p.color }}</td>
                <td>{{ p.talla }}</td>
                <td>{{ p.cantidad }}</td>
                <td>{{ Number(p.precioUnitario).toFixed(2) }} $</td>
                <td>{{ Number(p.total).toFixed(2) }} $</td>
                <td>{{ p.fecha }}</td>
              </tr>
            </tbody>
          </table>

          <p v-if="!loadingPedidos && !pedidosFiltrados.length">
            Todavía no has realizado pedidos.
          </p>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.pedidos-wrapper {
  padding: 2.5rem 1rem 3rem;
  display: flex;
  justify-content: center;
}

.pedidos-card {
  background: var(--cotton-light, #fcf5e9);
  border-radius: 20px;
  padding: 2rem 1.75rem;
  max-width: 1100px;
  width: 100%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
}

.pedidos-header {
  margin-bottom: 1.5rem;
}

.pedidos-header h2 {
  font-size: 1.6rem;
  margin-bottom: 0.3rem;
  color: var(--cotton-dark, #1c262e);
}

.pedidos-header p {
  color: #555;
  font-size: 0.95rem;
}

/* Tabs cliente */
.ped-tabs {
  display: inline-flex;
  gap: 0.5rem;
  border-radius: 999px;
  background: #e6e6e6;
  padding: 0.2rem;
  margin-bottom: 1.5rem;
}

.ped-tab-btn {
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

.ped-tab-btn.active {
  background: #ffffff;
  color: var(--cotton-dark, #1c262e);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

/* Panel general */
.ped-panel {
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

/* Tabla */
.ped-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
}

.ped-table thead {
  background: #f0f0f0;
}

.ped-table th,
.ped-table td {
  padding: 0.55rem 0.6rem;
  border-bottom: 1px solid #e4e4e4;
  text-align: left;
}

.ped-table th {
  color: #1c262e;
  font-weight: 600;
}

.ped-table td {
  color: #333333;
}

/* Catálogo */
.catalog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.prod-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 1rem;
  border: 1px solid #e2e2e2;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
}

.prod-card h4 {
  font-size: 1rem;
  margin-bottom: 0.25rem;
  color: var(--cotton-dark, #1c262e);
}

.prod-price {
  font-weight: 600;
  margin-bottom: 0.25rem;
  color: #1c262e;
}

.prod-stock {
  font-size: 0.85rem;
  color: #555;
  margin-bottom: 0.6rem;
}

.prod-btn {
  width: 100%;
  justify-content: center;
}

/* Bloque nuevo pedido */
.nuevo-pedido-panel {
  margin-top: 1.5rem;
  padding-top: 1.2rem;
  border-top: 1px solid #e2e2e2;
}

.nuevo-pedido-panel h3 {
  font-size: 1.05rem;
  margin-bottom: 0.5rem;
}

/* Aquí oscurecemos todo el texto de "Nuevo pedido" */
.nuevo-pedido-panel h3,
.nuevo-pedido-resumen,
.nuevo-pedido-panel p,
.nuevo-pedido-panel strong {
  color: #1c262e;
}

.nuevo-pedido-resumen {
  font-size: 0.9rem;
  margin-bottom: 0.8rem;
}

.nuevo-pedido-botones {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.nuevo-pedido-botones .cancelar {
  background: #6c757d;
  box-shadow: none;
}

/* MODAL de mensajes */
.alert-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.alert-box {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.5rem 2rem;
  width: min(400px, 90%);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
  text-align: center;
}

.alert-text {
  color: #1c262e;
  margin-bottom: 1rem;
  font-weight: 500;
}

.alert-btn {
  background: var(--cotton-accent, #e18b6b);
  color: #ffffff;
  border: none;
  border-radius: 999px;
  padding: 0.5rem 1.5rem;
  font-weight: 600;
  cursor: pointer;
}

.alert-btn:hover {
  background: var(--cotton-accent-hover, #d67a5d);
}

.alert-error {
  border-top: 5px solid #e74c3c;
}

.alert-success {
  border-top: 5px solid #27ae60;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
