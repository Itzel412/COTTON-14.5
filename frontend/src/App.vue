<script setup>
import { ref } from 'vue';

import LoginView from './views/LoginView.vue';
import DashboardView from './views/DashboardView.vue';
import PerfilView from './views/PerfilView.vue';
// importa los otros módulos que ya tengas
// import InventarioView from './views/InventarioView.vue';
// import PedidosView from './views/PedidosView.vue';
// import FacturasView from './views/FacturasView.vue';
// import ReclamosView from './views/ReclamosView.vue';
import HeaderBar from './components/HeaderBar.vue';

const currentUser = ref(null);
const currentModule = ref('dashboard');

const handleLoginSuccess = (user) => {
  currentUser.value = user;
  currentModule.value = 'dashboard';
};

const handleLogout = () => {
  currentUser.value = null;
  currentModule.value = 'dashboard';
};

const handleOpenModule = (modulo) => {
  currentModule.value = modulo;
};
</script>


<template>
  <div id="app">
    <!-- Si no hay usuario, mostrar login -->
    <LoginView v-if="!currentUser" @login-success="handleLoginSuccess" />

    <!-- Si hay usuario logueado, mostrar panel -->
    <div v-else>
      <HeaderBar
        :currentUser="currentUser"
        @go-home="currentModule = 'dashboard'"
        @logout="handleLogout"
        @open-profile="currentModule = 'perfiles'"
      />

      <main class="app-main">
        <DashboardView
          v-if="currentModule === 'dashboard'"
          :currentUser="currentUser"
          @open-module="handleOpenModule"
        />

        <PerfilView
          v-else-if="currentModule === 'perfiles'"
          :currentUser="currentUser"
        />

        <!-- EJEMPLOS: otros módulos -->
        <!--
        <InventarioView
          v-else-if="currentModule === 'inventario'"
          :currentUser="currentUser"
        />
        <PedidosView
          v-else-if="currentModule === 'pedidos'"
          :currentUser="currentUser"
        />
        <FacturasView
          v-else-if="currentModule === 'facturas'"
          :currentUser="currentUser"
        />
        <ReclamosView
          v-else-if="currentModule === 'reclamos'"
          :currentUser="currentUser"
        />
        -->
      </main>
    </div>
  </div>
</template>
