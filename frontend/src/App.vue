<script setup>
import { ref } from 'vue';

import LoginView from './views/LoginView.vue';
import DashboardView from './views/DashboardView.vue';
import PerfilView from './views/PerfilView.vue';
import InventarioView from './views/InventarioView.vue';
import PedidosView from './views/PedidosView.vue';
import FacturasView from './views/FacturasView.vue';
import ReclamosView from './views/ReclamosView.vue';
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
        <!-- DASHBOARD -->
        <DashboardView
          v-if="currentModule === 'dashboard'"
          :currentUser="currentUser"
          @open-module="handleOpenModule"
        />

        <!-- PERFIL -->
        <PerfilView
          v-else-if="currentModule === 'perfiles'"
          :currentUser="currentUser"
        />

        <!-- INVENTARIO (solo admin, pero ya lo controlas dentro de la vista) -->
        <InventarioView
          v-else-if="currentModule === 'inventario'"
          :currentUser="currentUser"
        />

        <!-- PEDIDOS / CATÁLOGO -->
        <PedidosView
          v-else-if="currentModule === 'pedidos'"
          :currentUser="currentUser"
        />

        <!-- FACTURAS -->
        <FacturasView
          v-else-if="currentModule === 'facturas'"
          :currentUser="currentUser"
        />

        <!-- RECLAMOS (AQUÍ ES DONDE FALTABA EL CASE) -->
        <ReclamosView
          v-else-if="currentModule === 'reclamos'"
          :currentUser="currentUser"
        />
      </main>
    </div>
  </div>
</template>
