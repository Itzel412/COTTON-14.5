<script setup>
<<<<<<< Updated upstream
import { RouterLink, RouterView } from 'vue-router'
import HelloWorld from './components/HelloWorld.vue'
=======
import { ref } from 'vue';

// Vistas existentes
import LoginView from './views/LoginView.vue';
import DashboardView from './views/DashboardView.vue';
import PerfilView from './views/PerfilView.vue';
import InventarioView from './views/InventarioView.vue';
import PedidosView from './views/PedidosView.vue';
import FacturasView from './views/FacturasView.vue';
import ReclamosView from './views/ReclamosView.vue';

// Componentes
import HeaderBar from './components/HeaderBar.vue';
// IMPORTAMOS EL NUEVO COMPONENTE DE TIENDA/CARRITO
import TiendaCliente from './components/TiendaCliente.vue';

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
>>>>>>> Stashed changes
</script>

<template>
  <header>
    <img alt="Vue logo" class="logo" src="@/assets/logo.svg" width="125" height="125" />

    <div class="wrapper">
      <HelloWorld msg="You did it!" />

<<<<<<< Updated upstream
      <nav>
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/about">About</RouterLink>
      </nav>
=======
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
>>>>>>> Stashed changes
    </div>
  </header>

  <RouterView />
</template>

<<<<<<< Updated upstream
<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

nav {
  width: 100%;
  font-size: 12px;
  text-align: center;
  margin-top: 2rem;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
}

nav a:first-of-type {
  border: 0;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }

  nav {
    text-align: left;
    margin-left: -1rem;
    font-size: 1rem;

    padding: 1rem 0;
    margin-top: 1rem;
  }
}
</style>
=======
<style>
/* Estilos globales básicos para asegurar que se vea bien */
body {
  margin: 0;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f4f4f4;
}

.app-main {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}
</style>
>>>>>>> Stashed changes
