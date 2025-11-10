<script setup>
import { ref } from 'vue';
import { loginRequest, registerClientePerfil } from '../data/api';

const emit = defineEmits(['login-success']);

const showLogin = ref(true);

const loginForm = ref({
  email: '',
  password: '',
});

const registerForm = ref({
  nombre: '',
  correo: '',
  clave: '',
  direccion: '',
  telefono: '',
});

const loginError = ref(null);
const registerError = ref(null);

const handleLogin = async () => {
  loginError.value = null;
  try {
    const user = await loginRequest(
      loginForm.value.email,
      loginForm.value.password,
    );
    emit('login-success', user);
  } catch (error) {
    loginError.value = error.message || 'Error al iniciar sesión.';
  }
};

const handleRegister = async () => {
  registerError.value = null;

  const faltantes = [];
  if (!registerForm.value.nombre) faltantes.push('Nombre');
  if (!registerForm.value.correo) faltantes.push('Correo');
  if (!registerForm.value.clave) faltantes.push('Contraseña');
  if (!registerForm.value.direccion) faltantes.push('Dirección');
  if (!registerForm.value.telefono) faltantes.push('Teléfono');

  if (faltantes.length > 0) {
    registerError.value = `Campo faltante: ${faltantes.join(', ')}.`;
    return;
  }

  try {
    const perfil = await registerClientePerfil({ ...registerForm.value });

    registerForm.value = {
      nombre: '',
      correo: '',
      clave: '',
      direccion: '',
      telefono: '',
    };

    emit('login-success', perfil);
  } catch (error) {
    registerError.value =
      error.message || 'Error al registrar el nuevo usuario.';
  }
};
</script>

<template>
  <div class="login-container">
    <div v-if="showLogin" class="login-box">
      <h1>COTTON</h1>
      <p>Inicia sesión para acceder a la tienda.</p>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>Correo electrónico</label>
          <input
            type="email"
            v-model="loginForm.email"
            required
            placeholder="tucorreo@ejemplo.com"
          />
        </div>

        <div class="form-group">
          <label>Contraseña</label>
          <input
            type="password"
            v-model="loginForm.password"
            required
            placeholder="********"
          />
        </div>

        <button type="submit" class="btn-ambos" style="width: 100%;">
          Iniciar sesión
        </button>

        <div v-if="loginError" class="login-error">
          {{ loginError }}
        </div>

        <p class="toggle-form" @click="showLogin = false">
          ¿No tienes cuenta? Regístrate aquí
        </p>
      </form>
    </div>

    <div v-else class="login-box">
      <h1>Crear cuenta de cliente</h1>
      <p>Regístrate para poder comprar franelas.</p>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>Nombre completo</label>
          <input type="text" v-model="registerForm.nombre" />
        </div>

        <div class="form-group">
          <label>Correo electrónico</label>
          <input type="email" v-model="registerForm.correo" />
        </div>

        <div class="form-group">
          <label>Contraseña</label>
          <input type="password" v-model="registerForm.clave" />
        </div>

        <div class="form-group">
          <label>Dirección</label>
          <input type="text" v-model="registerForm.direccion" />
        </div>

        <div class="form-group">
          <label>Teléfono</label>
          <input type="text" v-model="registerForm.telefono" />
        </div>

        <button type="submit" class="btn-ambos" style="width: 100%;">
          Registrarme
        </button>

        <div v-if="registerError" class="login-error">
          {{ registerError }}
        </div>

        <p class="toggle-form" @click="showLogin = true">
          ¿Ya tienes cuenta? Inicia sesión
        </p>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  background: linear-gradient(135deg, #1c262e 0%, #f7f0e8 100%);
}

.login-box {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid #e9cba7;
  padding: 2.25rem 2.5rem;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.12);
  text-align: center;
}

.login-box h1 {
  margin: 0 0 0.5rem;
  letter-spacing: 0.12em;
  font-size: 1.6rem;
  color: #1c262e;
}

.login-box p {
  margin: 0 0 1.5rem;
  font-size: 0.95rem;
  color: #4b4f52;
}

.form-group {
  margin-bottom: 1rem;
  text-align: left;
}

.form-group label {
  font-size: 0.9rem;
  display: block;
  margin-bottom: 0.25rem;
  color: #3a4145;
}

.form-group input {
  width: 100%;
  padding: 0.6rem 0.75rem;
  border-radius: 10px;
  border: 1px solid #d0d4d7;
  font-size: 0.9rem;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease,
    background-color 0.15s ease;
  background-color: #fafafa;
}

.form-group input:focus {
  border-color: #e18b6b;
  box-shadow: 0 0 0 2px rgba(225, 139, 107, 0.25);
  background-color: #ffffff;
}

.btn-ambos {
  width: 100%;
  border: none;
  border-radius: 999px;
  padding: 0.8rem 1.2rem;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  background: #e18b6b;
  color: #ffffff;
  text-align: center;
  transition: background 0.15s ease, transform 0.08s ease,
    box-shadow 0.15s ease;
  box-shadow: 0 8px 18px rgba(225, 139, 107, 0.35);
}

.btn-ambos:hover {
  background: #d67a5d;
  transform: translateY(-1px);
}

.btn-ambos:active {
  transform: translateY(0);
  box-shadow: 0 4px 10px rgba(225, 139, 107, 0.35);
}

.login-error {
  margin-top: 0.75rem;
  font-size: 0.85rem;
  color: #b12b3b;
  background: #ffe6ea;
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
}

.toggle-form {
  margin-top: 1.25rem;
  font-size: 0.9rem;
  color: #e18b6b;
  cursor: pointer;
}

.toggle-form:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-box {
    padding: 1.75rem 1.5rem;
  }
}
</style>

