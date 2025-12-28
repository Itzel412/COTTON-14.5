<script setup>
import { computed } from 'vue';
import ModuleCard from '../components/ModuleCard.vue';

const props = defineProps({
  currentUser: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['open-module']);

const esAdmin = computed(
  () => props.currentUser && props.currentUser.rol === 'ADMIN',
);

const nombreCorto = computed(() => {
  const nombre = props.currentUser?.nombre || '';
  const partes = nombre.split(' ');
  return partes[0] || nombre;
});

const abrirModulo = (modulo) => {
  emit('open-module', modulo);
};
</script>

<template>
  <section class="dashboard">
    <div class="dashboard-header">
      <template v-if="esAdmin">
        <h2>¿Qué módulo quieres gestionar hoy?</h2>
        <p>Administra la tienda de franelas desde estos módulos.</p>
      </template>

      <template v-else>
        <h2>¿Qué quieres hacer hoy en Cotton?</h2>
      </template>
    </div>

    <div v-if="esAdmin" class="dashboard-grid">
      <ModuleCard
        title="Usuarios"
        description="Crear y administrar cuentas de clientes y administradores."
        @click="abrirModulo('perfiles')"
      />

      <ModuleCard
        title="Inventario"
        description="Registra y consulta los productos disponibles."
        @click="abrirModulo('inventario')"
      />

      <ModuleCard
        title="Pedidos"
        description="Consultar los pedidos realizados."
        @click="abrirModulo('pedidos')"
      />

      <ModuleCard
        title="Facturación"
        description="Crea y consulta las facturas."
        @click="abrirModulo('facturas')"
      />

      <ModuleCard
        title="Reclamos"
        description="Modifica los reclamos de los clientes."
        @click="abrirModulo('reclamos')"
      />
    </div>

    <div v-else class="dashboard-grid">

      <ModuleCard
        title="Catalogo"
        description="Revisa los articulos disponibles en la tienda, realiza tu pedido."
        @click="abrirModulo('pedidos')"
      />

      <ModuleCard
        title="Mis facturas"
        description="Consulta tus facturas anteriores."
        @click="abrirModulo('facturas')"
      />

      <ModuleCard
        title="Reclamos"
        description="Reporta problemas o dudas sobre tus compras."
        @click="abrirModulo('reclamos')"
      />
    </div>
  </section>
</template>

<style scoped>
.dashboard {
  padding: 2.5rem 1rem 3rem;
  background-color: #fdf9f5;
  border-radius: 20px;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 2rem;
}

.dashboard-header h2 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  color: var(--cotton-dark, #1c262e);
}

.dashboard-header p {
  color: #555;
  font-size: 0.95rem;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.5rem;
}
</style>
