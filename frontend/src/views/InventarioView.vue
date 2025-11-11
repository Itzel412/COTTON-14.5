<script setup>
import { onMounted, ref, computed } from 'vue';
import { getProductos, createPedido } from '../data/api';

const props = defineProps({
  currentUser: {
    type: Object,
    required: true,
  },
});

const esAdmin = computed(
  () => props.currentUser && props.currentUser.rol === 'ADMIN',
);

const productos = ref([]);
const loading = ref(false);
const error = ref(null);

// Para el formulario de pedido del usuario
const productoSeleccionado = ref(null);
const cantidad = ref(1);
const mensajeOk = ref(null);

const cargarProductos = async () => {
  loading.value = true;
  error.value = null;
  try {
    productos.value = await getProductos();
  } catch (e) {
    error.value = e.message || 'Error al cargar productos';
  } finally {
    loading.value = false;
  }
};

const seleccionarProducto = (producto) => {
  productoSeleccionado.value = producto;
  cantidad.value = 1;
  mensajeOk.value = null;
  error.value = null;
};

const realizarPedido = async () => {
  if (!productoSeleccionado.value) return;

  // Validaciones de front (además de las del back)
  if (cantidad.value < 1 || cantidad.value > 10) {
    error.value = 'La cantidad debe estar entre 1 y 10.';
    return;
  }

  if (cantidad.value > productoSeleccionado.value.stock) {
    error.value = 'No hay suficiente stock para esa cantidad.';
    return;
  }

  error.value = null;
  mensajeOk.value = null;

  const prod = productoSeleccionado.value;

  // Usamos el correo como identificador del usuario
  const usuario =
    props.currentUser?.correo || props.currentUser?.nombre || 'desconocido';

  const hoy = new Date().toISOString().slice(0, 10); // YYYY-MM-DD

  const nuevoPedido = {
    id: 0, // el back lo genera
    usuario,
    idProducto: prod.id,
    nombre: prod.nombre,
    color: prod.color,
    talla: prod.talla,
    cantidad: cantidad.value,
    precioUnitario: prod.precio,
    total: 0, // el back lo calcula -> cantidad * precio
    fecha: hoy,
  };

  try {
    await createPedido(nuevoPedido);
    mensajeOk.value = 'Pedido registrado exitosamente.';

    // Opcional: actualizar stock en pantalla
    await cargarProductos();
    // Volver a seleccionar el producto para mostrarlo actualizado (si sigue existiendo)
    const actualizado = productos.value.find((p) => p.id === prod.id);
    productoSeleccionado.value = actualizado || null;
  } catch (e) {
    error.value = e.message || 'Error al registrar el pedido.';
  }
};

onMounted(() => {
  cargarProductos();
});
</script>

<template>
  <section>
    <!-- ===== VISTA ADMIN: solo ver catálogo ===== -->
    <div v-if="esAdmin">
      <h2 style="margin-bottom: 0.5rem;">Catálogo de productos</h2>
      <p style="margin-bottom: 1.5rem;">
        Vista solo de lectura. Los pedidos se consultan en el módulo
        <strong>Pedidos</strong>.
      </p>

      <p v-if="loading">Cargando productos...</p>
      <p v-if="error" style="color: #b12b3b;">{{ error }}</p>

      <table
        v-if="!loading && productos.length"
        style="width: 100%; border-collapse: collapse; font-size: 0.9rem;"
      >
        <thead>
          <tr>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">ID</th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Nombre
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Color
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Talla
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Precio
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Stock
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in productos" :key="p.id">
            <td style="padding: 0.5rem;">{{ p.id }}</td>
            <td style="padding: 0.5rem;">{{ p.nombre }}</td>
            <td style="padding: 0.5rem;">{{ p.color }}</td>
            <td style="padding: 0.5rem;">{{ p.talla }}</td>
            <td style="padding: 0.5rem;">{{ p.precio.toFixed(2) }} $</td>
            <td style="padding: 0.5rem;">{{ p.stock }}</td>
          </tr>
        </tbody>
      </table>

      <p v-if="!loading && !productos.length">
        No hay productos registrados en el inventario.
      </p>
    </div>

    <!-- ===== VISTA USUARIO: catálogo + crear pedido ===== -->
    <div v-else>
      <h2 style="margin-bottom: 0.5rem;">Catálogo de franelas</h2>
      <p style="margin-bottom: 1.5rem;">
        Selecciona una franela y completa la cantidad para realizar tu pedido.
      </p>

      <p v-if="loading">Cargando productos...</p>
      <p v-if="error" style="color: #b12b3b;">{{ error }}</p>

      <div class="catalog-grid" v-if="!loading && productos.length">
        <div
          v-for="p in productos"
          :key="p.id"
          class="product-card"
          @click="seleccionarProducto(p)"
          :class="{
            selected: productoSeleccionado && productoSeleccionado.id === p.id,
          }"
        >
          <!-- Aquí podrías usar tus imágenes reales por color si quieres -->
          <!-- Ejemplo: <img :src="`/img/${p.color.toLowerCase()}.png`" class="product-image-real" /> -->
          <div class="product-image">
            {{ p.color.charAt(0) }}
          </div>
          <h3>{{ p.nombre }}</h3>
          <p class="product-variant">
            Color: {{ p.color }} · Talla: {{ p.talla }}
          </p>
          <p class="product-price">{{ p.precio.toFixed(2) }} $</p>
          <p class="product-stock">
            Stock: <strong>{{ p.stock }}</strong>
          </p>
        </div>
      </div>

      <p v-if="!loading && !productos.length">
        No hay productos disponibles en este momento.
      </p>

      <!-- FORMULARIO DE PEDIDO -->
      <div v-if="productoSeleccionado" class="pedido-panel">
        <h3>Realizar pedido</h3>
        <p style="font-size: 0.9rem; margin-bottom: 0.5rem;">
          Producto seleccionado:
          <strong>{{ productoSeleccionado.nombre }}</strong>
          ({{ productoSeleccionado.color }} - {{ productoSeleccionado.talla }})
        </p>

        <div class="form-group">
          <label>Cantidad (máx. 10)</label>
          <input
            type="number"
            min="1"
            max="10"
            v-model.number="cantidad"
          />
          <small style="font-size: 0.8rem; color: #555;">
            Stock disponible: {{ productoSeleccionado.stock }}
          </small>
        </div>

        <p style="margin-bottom: 0.75rem; font-size: 0.9rem;">
          Precio unitario:
          <strong>{{ productoSeleccionado.precio.toFixed(2) }} $</strong>
          <br />
          Total aproximado:
          <strong>{{
            (cantidad * productoSeleccionado.precio).toFixed(2)
          }} $</strong>
        </p>

        <button class="btn-ambos" @click="realizarPedido">
          Confirmar pedido
        </button>

        <div v-if="mensajeOk" style="margin-top: 0.75rem; color: #1b7965;">
          {{ mensajeOk }}
        </div>
        <div v-if="error" style="margin-top: 0.75rem; color: #b12b3b;">
          {{ error }}
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.catalog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.product-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 1rem;
  border: 1px solid #dddddd;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease,
    border-color 0.15s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  border-color: var(--cotton-soft, #e9cba7);
}

.product-card.selected {
  border-color: var(--cotton-accent, #e18b6b);
  box-shadow: 0 0 0 2px rgba(225, 139, 107, 0.2);
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 999px;
  background: #f0f0f0;
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #333;
}

.product-variant {
  font-size: 0.85rem;
  color: #555;
}

.product-price {
  font-size: 0.95rem;
  font-weight: 600;
  margin-top: 0.4rem;
}

.product-stock {
  font-size: 0.8rem;
  color: #777;
  margin-top: 0.2rem;
}

.pedido-panel {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.5rem;
  border: 1px solid #dddddd;
  max-width: 450px;
}
</style>
