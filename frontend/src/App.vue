<script setup>
import { ref } from 'vue';
import LoginView from './views/LoginView.vue';
import HeaderBar from './components/HeaderBar.vue';
import DashboardView from './views/DashboardView.vue';
import PerfilView from './views/PerfilView.vue';

const isLoggedIn = ref(false);
const currentUser = ref(null);
const currentView = ref('dashboard'); 

const handleLoginSuccess = (user) => {
  isLoggedIn.value = true;
  currentUser.value = user;
  currentView.value = 'dashboard';
};

const handleLogout = () => {
  isLoggedIn.value = false;
  currentUser.value = null;
  currentView.value = 'dashboard';
};

const goToView = (viewName) => {
  currentView.value = viewName;
};
</script>

<template>
  <div id="app">
    <LoginView v-if="!isLoggedIn" @login-success="handleLoginSuccess" />

    <div v-else>
      <HeaderBar
        :current-user="currentUser"
        @logout="handleLogout"
        @go-home="goToView('dashboard')"
      />

      <main class="app-main">
        <DashboardView
          v-if="currentView === 'dashboard'"
          :current-user="currentUser"
          @go-to="goToView"
        />

        <PerfilView
          v-else-if="currentView === 'perfil'"
          :current-user="currentUser"
        />

        <p v-else style="margin-top: 2rem;" >
          (Vista {{ currentView }} aún no implementada.)
        </p>
      </main>   

    </div>
  </div>
</template>
