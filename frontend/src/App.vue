<script setup>
import { ref } from 'vue';

// --- 1. IMPORTACIÓN DE VISTAS ---
import LoginView from './views/LoginView.vue';
import DashboardView from './views/DashboardView.vue';
import PerfilView from './views/PerfilView.vue';
import InventarioView from './views/InventarioView.vue';
import PedidosView from './views/PedidosView.vue'; 
import FacturasView from './views/FacturasView.vue'; 
import ReclamosView from './views/ReclamosView.vue'; 

// --- 2. IMPORTACIÓN DE COMPONENTES ---
import HeaderBar from './components/HeaderBar.vue'; 
import TiendaCliente from './components/TiendaCliente.vue'; 

// --- 3. ESTADO DE LA APP ---
const currentUser = ref(null);
const currentModule = ref('dashboard');

// --- 4. FUNCIONES DE LÓGICA ---
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
  <LoginView 
    v-if="!currentUser" 
    @login-success="handleLoginSuccess" 
  />

  <div v-else class="app-container">
    
    <HeaderBar 
      :currentUser="currentUser" 
      @logout="handleLogout" 
      @go-home="handleOpenModule('dashboard')"
      @open-profile="handleOpenModule('perfiles')"
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

      <TiendaCliente
        v-else-if="currentModule === 'pedidos' && currentUser.rol === 'CLIENTE'"
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

    </main>
  </div>
</template>

<style>
/* ESTILOS GLOBALES */
body {
  margin: 0;
  padding: 0;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f4f4f4;
  color: #333;
}

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-main {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}
</style>