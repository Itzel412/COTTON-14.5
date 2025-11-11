<script setup>
import { onMounted, ref, computed } from 'vue';
import { getPedidos } from '../data/api';

const props = defineProps({
  currentUser: {
    type: Object,
    required: true,
  },
});

const esAdmin = computed(
  () => props.currentUser && props.currentUser.rol === 'ADMIN',
);

const pedidos = ref([]);
const loading = ref(false);
const error = ref(null);

const cargarPedidos = async () => {
  loading.value = true;
  error.value = null;
  try {
    const todos = await getPedidos();

    if (esAdmin.value) {
      // Admin ve todos
      pedidos.value = todos;
    } else {
      // Usuario solo ve los suyos
      const usuarioClave =
        props.currentUser?.correo || props.currentUser?.nombre || '';
      pedidos.value = todos.filter((p) => p.usuario === usuarioClave);
    }
  } catch (e) {
    error.value = e.message || 'Error al cargar pedidos';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  cargarPedidos();
});
</script>

<template>
  <section>
    <!-- ===== ADMIN ===== -->
    <div v-if="esAdmin">
      <h2 style="margin-bottom: 0.5rem;">Pedidos registrados</h2>
      <p style="margin-bottom: 1.5rem;">
        Consulta los pedidos realizados por los clientes.
      </p>

      <p v-if="loading">Cargando pedidos...</p>
      <p v-if="error" style="color: #b12b3b;">{{ error }}</p>

      <table
        v-if="!loading && pedidos.length"
        style="width: 100%; border-collapse: collapse; font-size: 0.9rem;"
      >
        <thead>
          <tr>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">ID</th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Usuario
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Producto
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Color
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Talla
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Cantidad
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Precio unit.
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Total
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Fecha
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in pedidos" :key="p.id">
            <td style="padding: 0.5rem;">{{ p.id }}</td>
            <td style="padding: 0.5rem;">{{ p.usuario }}</td>
            <td style="padding: 0.5rem;">{{ p.nombre }}</td>
            <td style="padding: 0.5rem;">{{ p.color }}</td>
            <td style="padding: 0.5rem;">{{ p.talla }}</td>
            <td style="padding: 0.5rem; text-align: center;">
              {{ p.cantidad }}
            </td>
            <td style="padding: 0.5rem;">
              {{ Number(p.precioUnitario).toFixed(2) }} $
            </td>
            <td style="padding: 0.5rem;">
              {{ Number(p.total).toFixed(2) }} $
            </td>
            <td style="padding: 0.5rem;">{{ p.fecha }}</td>
          </tr>
        </tbody>
      </table>

      <p v-if="!loading && !pedidos.length">
        No hay pedidos registrados aún.
      </p>
    </div>

    <!-- ===== USUARIO NORMAL ===== -->
    <div v-else>
      <h2 style="margin-bottom: 0.5rem;">Mis pedidos</h2>
      <p style="margin-bottom: 1.5rem;">
        Aquí puedes ver los pedidos que has realizado en Cotton.
      </p>

      <p v-if="loading">Cargando pedidos...</p>
      <p v-if="error" style="color: #b12b3b;">{{ error }}</p>

      <table
        v-if="!loading && pedidos.length"
        style="width: 100%; border-collapse: collapse; font-size: 0.9rem;"
      >
        <thead>
          <tr>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">ID</th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Producto
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Color
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Talla
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Cantidad
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Precio unit.
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Total
            </th>
            <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
              Fecha
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in pedidos" :key="p.id">
            <td style="padding: 0.5rem;">{{ p.id }}</td>
            <td style="padding: 0.5rem;">{{ p.nombre }}</td>
            <td style="padding: 0.5rem;">{{ p.color }}</td>
            <td style="padding: 0.5rem;">{{ p.talla }}</td>
            <td style="padding: 0.5rem; text-align: center;">
              {{ p.cantidad }}
            </td>
            <td style="padding: 0.5rem;">
              {{ Number(p.precioUnitario).toFixed(2) }} $
            </td>
            <td style="padding: 0.5rem;">
              {{ Number(p.total).toFixed(2) }} $
            </td>
            <td style="padding: 0.5rem;">{{ p.fecha }}</td>
          </tr>
        </tbody>
      </table>

      <p v-if="!loading && !pedidos.length">
        Aún no has realizado ningún pedido.
      </p>
    </div>
  </section>
</template>
