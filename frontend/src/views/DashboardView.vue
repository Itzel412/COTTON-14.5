<script setup>
const props = defineProps({
  currentUser: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['go-to']);
</script>

<template>
  <section>
    <h2 style="margin-bottom: 1rem;">
      ¿Qué módulo quieres gestionar hoy?
    </h2>

    <div class="dashboard-grid">
      <div class="module-card" @click="emit('go-to', 'perfil')">
        <h2>Gestión de Perfiles</h2>
        <p>Crear, editar, ver y eliminar usuarios.</p>
      </div>

      <div class="module-card" @click="emit('go-to', 'inventario')">
        <h2>Inventario</h2>
        <p>Gestiona las franelas disponibles para la venta.</p>
      </div>

      <div class="module-card" @click="emit('go-to', 'pedidos')">
        <h2>Pedidos</h2>
        <p>Realizar y consultar pedidos de clientes.</p>
      </div>

      <div class="module-card" @click="emit('go-to', 'facturas')">
        <h2>Facturas</h2>
        <p>Crear facturas con impuestos y montos.</p>
      </div>

      <div class="module-card" @click="emit('go-to', 'reclamos')">
        <h2>Reclamos</h2>
        <p>Registrar y actualizar el estado de reclamos.</p>
      </div>
    </div>
  </section>
</template>
