<script setup>
import { ref } from 'vue';

import LoginView from './views/LoginView.vue';
import DashboardView from './views/DashboardView.vue';
import PerfilView from './views/PerfilView.vue';
import InventarioView from './views/InventarioView.vue';
import PedidosView from './views/PedidosView.vue';
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
    <LoginView v-if="!currentUser" @login-success="handleLoginSuccess" />

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

        <InventarioView
          v-else-if="currentModule === 'inventario'"
          :currentUser="currentUser"
        />

        <PedidosView
          v-else-if="currentModule === 'pedidos'"
          :currentUser="currentUser"
        />

        <!-- Si luego agregas estos, solo quitas los comentarios -->
        <!--
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
