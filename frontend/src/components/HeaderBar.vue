<script setup>
import { computed } from 'vue';

const props = defineProps({
  currentUser: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['logout', 'go-home', 'open-profile']);

const inicialPerfil = computed(() => {
  const nombre = props.currentUser?.nombre || 'C';
  return nombre.charAt(0).toUpperCase();
});
</script>

<template>
  <header class="cotton-header">
    <div class="header-left">
      <div class="header-brand" @click="emit('go-home')">
        <img src="/Logo.png" alt="Logo Cotton" class="header-logo" />
      </div>

      <button class="profile-link" @click="emit('open-profile')">
        <div class="profile-avatar">
          {{ inicialPerfil }}
        </div>
        <span class="profile-text">Ver perfil</span>
      </button>
    </div>

    <div class="header-actions">
      <button class="btn-header" @click="emit('go-home')">Inicio</button>
      <button class="btn-header logout" @click="emit('logout')">
        Cerrar sesión
      </button>
    </div>
  </header>
</template>

<style scoped>
.cotton-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #FCF5E9;
  padding: 0.6rem 2rem;
  height: 70px;
  border-bottom: 1px solid #e4d9c9;
}


.header-left {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  cursor: pointer;
}

.header-logo {
  width: 70px; 
  height: 65px;
  object-fit: contain;
}

.profile-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 0.3rem 0.6rem;
  border-radius: 999px;
  transition: background 0.15s ease;
}

.profile-link:hover {
  background: rgba(0, 0, 0, 0.03);
}

.profile-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #1c262e;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  font-weight: 600;
}

.profile-text {
  font-size: 0.9rem;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.btn-header {
  background: #e1e7ec;
  color: #1c262e;
  border: none;
  border-radius: 999px;
  padding: 0.45rem 1.1rem;
  font-size: 0.9rem;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s ease, transform 0.07s ease;
}

.btn-header:hover {
  background: #d2dde6;
  transform: translateY(-1px);
}

.btn-header.logout {
  background: #e18b6b;
  color: #ffffff;
}

.btn-header.logout:hover {
  background: #dd8463;
}
</style>
