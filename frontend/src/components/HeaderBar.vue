<!-- src/components/HeaderBar.vue -->
<script setup>
const props = defineProps({
  currentUser: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['logout', 'go-home']);
</script>

<template>
  <header class="app-header">
    <div>
      <h1>Cotton - Panel</h1>
      <small
        >Bienvenido,
        {{ currentUser ? currentUser.nombre : 'Usuario' }}</small
      >
    </div>

    <div class="app-header-user">
      <span v-if="currentUser">Rol: {{ currentUser.rol }}</span>
      <button class="btn-ambos" @click="emit('go-home')">
        Inicio
      </button>
      <button
        class="btn-ambos"
        style="margin-left: 0.5rem; background: #c1121f;"
        @click="emit('logout')"
      >
        Cerrar sesión
      </button>
    </div>
  </header>
</template>
