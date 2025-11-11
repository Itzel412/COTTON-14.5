<script setup>
import { ref, computed, onMounted } from 'vue';
import { getProductos, createProducto } from '../data/api';

const props = defineProps({
  currentUser: {
    type: Object,
    required: true,
  },
});

const esAdmin = computed(() => props.currentUser?.rol === 'ADMIN');

const productos = ref([]);
const loading = ref(false);
const error = ref(null);

// ya no mostramos mensaje fijo arriba, ahora usamos modal
const mensajeProducto = ref(null);

const modoAdmin = ref('ver');
const pasoProducto = ref('form');
const productoParaConfirmar = ref(null);

const nuevoProducto = ref({
  color: 'Blanco',
  talla: 'M',
  precio: 0,
  stock: 0,
});

const COLORES = ['Blanco', 'Negro', 'Rojo', 'Azul', 'Amarillo', 'Verde', 'Morado'];
const TALLAS = ['S', 'M', 'L', 'XL'];

// -------- Modal de "warning" / OK ----------
const dialogVisible = ref(false);
const dialogMessage = ref('');

const abrirDialogo = (msg) => {
  dialogMessage.value = msg;
  dialogVisible.value = true;
};
const cerrarDialogo = () => {
  dialogVisible.value = false;
};
// -------------------------------------------

const cargarProductos = async () => {
  if (!esAdmin.value) return;
  loading.value = true;
  error.value = null;

  try {
    productos.value = await getProductos();
  } catch (e) {
    error.value = e.message || 'Error al cargar los productos.';
  } finally {
    loading.value = false;
  }
};

const prepararConfirmacionProducto = () => {
  error.value = null;
  mensajeProducto.value = null;

  const p = nuevoProducto.value;
  const faltantes = [];
  if (!p.color) faltantes.push('Color');
  if (!p.talla) faltantes.push('Talla');
  if (p.precio <= 0) faltantes.push('Precio (> 0)');
  if (p.stock < 0) faltantes.push('Stock (≥ 0)');

  if (faltantes.length > 0) {
    error.value = `Campo(s) inválido(s): ${faltantes.join(', ')}.`;
    return;
  }

  if (!COLORES.includes(p.color)) {
    error.value = 'Selecciona un color válido.';
    return;
  }

  if (!TALLAS.includes(p.talla)) {
    error.value = 'La talla debe ser S, M, L o XL.';
    return;
  }

  productoParaConfirmar.value = { ...p };
  pasoProducto.value = 'confirm';
};

const registrarProductoConfirmado = async () => {
  if (!productoParaConfirmar.value) return;

  error.value = null;
  try {
    const ok = await createProducto({ ...productoParaConfirmar.value });

    if (!ok) {
      error.value = 'El backend no pudo registrar el producto.';
      return;
    }

    // mostramos modal tipo "warning" con OK
    abrirDialogo('Prenda registrada satisfactoriamente.');

    // reseteamos el formulario
    nuevoProducto.value = {
      color: 'Blanco',
      talla: 'M',
      precio: 0,
      stock: 0,
    };
    productoParaConfirmar.value = null;
    pasoProducto.value = 'form';

    await cargarProductos();
  } catch (e) {
    error.value = e.message || 'Error al registrar el producto.';
  }
};

const cancelarConfirmacion = () => {
  productoParaConfirmar.value = null;
  pasoProducto.value = 'form';
  // también usamos modal para el mensaje de cancelación
  abrirDialogo('Se canceló el registro. No se guardaron cambios.');
};

onMounted(() => {
  if (esAdmin.value) {
    cargarProductos();
  }
});
</script>

<template>
  <section class="inventario-wrapper">
    <div class="inventario-card" v-if="esAdmin">
      <header class="inventario-header">
        <h2>Gestión de inventario</h2>
        <p>Registra nuevas franelas y consulta el inventario actual.</p>
      </header>

      <div v-if="error" class="inv-alert error">
        {{ error }}
      </div>

      <div class="inv-tabs">
        <button
          type="button"
          class="inv-tab-btn"
          :class="{ active: modoAdmin === 'ver' }"
          @click="modoAdmin = 'ver'"
        >
          Ver productos
        </button>
        <button
          type="button"
          class="inv-tab-btn"
          :class="{ active: modoAdmin === 'crear' }"
          @click="modoAdmin = 'crear'"
        >
          Registrar producto
        </button>
      </div>

      <!-- VER PRODUCTOS -->
      <div v-if="modoAdmin === 'ver'" class="inv-panel">
        <h3 class="panel-title">Productos en inventario</h3>

        <p v-if="loading">Cargando productos...</p>

        <table
          v-else-if="productos.length"
          class="inv-table"
        >
          <thead>
            <tr>
              <th>Color</th>
              <th>Talla</th>
              <th>Precio</th>
              <th>Stock</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in productos" :key="p.id">
              <td>{{ p.color }}</td>
              <td>{{ p.talla }}</td>
              <td>{{ Number(p.precio).toFixed(2) }} $</td>
              <td>{{ p.stock }}</td>
            </tr>
          </tbody>
        </table>

        <p
          v-else
          style="color: #1b1b1b; font-weight: 500; text-align: center; margin-top: 1rem;"
        >
          No hay productos registrados en el inventario.
        </p>
      </div>

      <!-- REGISTRAR PRODUCTO -->
      <div v-else class="inv-panel">
        <h3 class="panel-title">Registrar nuevo producto</h3>

        <div v-if="pasoProducto === 'form'">
          <form @submit.prevent="prepararConfirmacionProducto">
            <div class="form-row">
              <div class="form-group">
                <label>Color</label>
                <select v-model="nuevoProducto.color">
                  <option v-for="c in COLORES" :key="c" :value="c">
                    {{ c }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label>Talla</label>
                <select v-model="nuevoProducto.talla">
                  <option v-for="t in TALLAS" :key="t" :value="t">
                    {{ t }}
                  </option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Precio (USD)</label>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  v-model.number="nuevoProducto.precio"
                />
              </div>

              <div class="form-group">
                <label>Stock</label>
                <input
                  type="number"
                  min="0"
                  v-model.number="nuevoProducto.stock"
                />
              </div>
            </div>

            <button type="submit" class="btn-ambos">
              Validar y continuar
            </button>
          </form>
        </div>

        <!-- BLOQUE DE CONFIRMACIÓN (texto ya oscuro) -->
        <div v-else class="confirm-block">
          <h4>Confirmar datos de la prenda</h4>
          <p><strong>Color:</strong> {{ productoParaConfirmar.color }}</p>
          <p><strong>Talla:</strong> {{ productoParaConfirmar.talla }}</p>
          <p><strong>Precio:</strong> {{ productoParaConfirmar.precio }} $</p>
          <p><strong>Stock:</strong> {{ productoParaConfirmar.stock }}</p>

          <div class="confirm-buttons">
            <button class="btn-ambos" @click="registrarProductoConfirmado">
              Confirmar y registrar
            </button>
            <button
              class="btn-ambos btn-secondary"
              @click="cancelarConfirmacion"
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL DE MENSAJE -->
    <div v-if="dialogVisible" class="inv-dialog-backdrop">
      <div class="inv-dialog">
        <p>{{ dialogMessage }}</p>
        <button class="btn-ambos" @click="cerrarDialogo">
          OK
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.inventario-wrapper {
  padding: 2.5rem 1rem 3rem;
  display: flex;
  justify-content: center;
}

.inventario-card {
  background: var(--cotton-light, #fcf5e9);
  border-radius: 20px;
  padding: 2rem 1.75rem;
  max-width: 1000px;
  width: 100%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
}

.inventario-header {
  margin-bottom: 1.5rem;
}

.inventario-header h2 {
  font-size: 1.6rem;
  margin-bottom: 0.3rem;
  color: var(--cotton-dark, #1c262e);
}

.inventario-header p {
  color: #555;
  font-size: 0.95rem;
}

.inv-alert {
  padding: 0.6rem 0.8rem;
  border-radius: 10px;
  font-size: 0.9rem;
  margin-bottom: 0.8rem;
}
.inv-alert.error {
  background: #f8d7da;
  color: #842029;
}

.inv-tabs {
  display: inline-flex;
  gap: 0.5rem;
  border-radius: 999px;
  background: #e6e6e6;
  padding: 0.2rem;
  margin-bottom: 1.5rem;
}
.inv-tab-btn {
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
.inv-tab-btn.active {
  background: #ffffff;
  color: var(--cotton-dark, #1c262e);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.inv-panel {
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

.form-row {
  display: flex;
  gap: 1rem;
}

/* tabla */
.inv-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
}
.inv-table thead {
  background: #f0f0f0;
}
.inv-table th,
.inv-table td {
  padding: 0.55rem 0.6rem;
  border-bottom: 1px solid #e4e4e4;
  text-align: left;
}
.inv-table th {
  color: #1c262e;
  font-weight: 600;
}
.inv-table td {
  color: #333333;
}

/* bloque de confirmación */
.confirm-block {
  color: #1c262e;
}
.confirm-block h4 {
  margin-bottom: 0.75rem;
}
.confirm-block p,
.confirm-block strong {
  color: #1c262e;
}
.confirm-buttons {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}
.btn-secondary {
  background: #6c757d;
  box-shadow: none;
}

/* modal */
.inv-dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.inv-dialog {
  background: #ffffff;
  padding: 1.5rem 1.75rem;
  border-radius: 16px;
  max-width: 400px;
  width: 90%;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
}
.inv-dialog p {
  margin-bottom: 1rem;
  color: #1c262e;
}
</style>
