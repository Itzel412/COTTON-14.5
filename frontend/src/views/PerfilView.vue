<script setup>
import { onMounted, ref, computed } from 'vue';
import { getPerfiles, createPerfil } from '../data/api';

const props = defineProps({
  currentUser: {
    type: Object,
    default: null,
  },
});

const esAdmin = computed(
  () => props.currentUser && props.currentUser.rol === 'ADMIN',
);

const modo = ref('crear'); 

const perfiles = ref([]);
const loading = ref(false);
const error = ref(null);
const mensajeCrear = ref(null);
const perfilSeleccionado = ref(null);

const pasoCrear = ref('form'); 

const nuevoPerfil = ref({
  nombre: '',
  correo: '',
  clave: '',
  direccion: '',
  telefono: '',
  rol: 'CLIENTE',
});

const perfilParaConfirmar = ref(null);

const cargarPerfiles = async () => {
  if (!esAdmin.value) return;
  loading.value = true;
  error.value = null;
  try {
    perfiles.value = await getPerfiles();
  } catch (e) {
    error.value = e.message || 'Error al cargar perfiles';
  } finally {
    loading.value = false;
  }
};

const seleccionarPerfil = (perfil) => {
  perfilSeleccionado.value = perfil;
};

const validarYPrepararConfirmacion = async () => {
  mensajeCrear.value = null;
  error.value = null;

  const faltantes = [];
  if (!nuevoPerfil.value.nombre) faltantes.push('Nombre');
  if (!nuevoPerfil.value.correo) faltantes.push('Correo');
  if (!nuevoPerfil.value.clave) faltantes.push('Clave');
  if (!nuevoPerfil.value.direccion) faltantes.push('Dirección');
  if (!nuevoPerfil.value.telefono) faltantes.push('Teléfono');

  if (faltantes.length > 0) {
    error.value = `Campo faltante: ${faltantes.join(', ')}.`;
    return;
  }

  if (!perfiles.value.length) {
    await cargarPerfiles();
  }

  const correoRepetido = perfiles.value.some(
    (p) =>
      p.correo &&
      p.correo.toLowerCase() === nuevoPerfil.value.correo.toLowerCase(),
  );
  if (correoRepetido) {
    error.value = 'El correo electrónico ya está en uso';
    return;
  }

  perfilParaConfirmar.value = { ...nuevoPerfil.value };
  pasoCrear.value = 'confirm';
  mensajeCrear.value =
    'Verifica que estos sean los datos correctos antes de registrar al usuario.';
};

const confirmarYRegistrar = async () => {
  if (!perfilParaConfirmar.value) return;
  try {
    error.value = null;
    perfiles.value = await createPerfil({ ...perfilParaConfirmar.value });
    nuevoPerfil.value = {
      nombre: '',
      correo: '',
      clave: '',
      direccion: '',
      telefono: '',
      rol: 'CLIENTE',
    };
    perfilParaConfirmar.value = null;
    pasoCrear.value = 'form';
    mensajeCrear.value = 'Usuario registrado exitosamente.';
  } catch (e) {
    error.value = e.message || 'Error al registrar el usuario';
  }
};

const cancelarRegistro = () => {
  perfilParaConfirmar.value = null;
  pasoCrear.value = 'form';
  mensajeCrear.value =
    'Se canceló el registro. No se guardaron los datos.';
};

onMounted(() => {
  if (esAdmin.value) {
    cargarPerfiles();
  }
});
</script>

<template>
  <section class="perfil-wrapper">
    <div v-if="esAdmin" class="perfil-card">
      <header class="perfil-header">
        <h2>Perfiles</h2>
        <p>Aqui puedes crear o cunsultar los perfiles de los usuarios.</p>
      </header>

      <div v-if="error" class="perfil-alert error">
        {{ error }}
      </div>
      <div v-if="mensajeCrear" class="perfil-alert ok">
        {{ mensajeCrear }}
      </div>

      <div class="perfil-tabs">
        <button
          type="button"
          class="tab-btn"
          :class="{ active: modo === 'crear' }"
          @click="modo = 'crear'"
        >
          Crear perfil
        </button>
        <button
          type="button"
          class="tab-btn"
          :class="{ active: modo === 'ver' }"
          @click="modo = 'ver'"
        >
          Ver perfiles
        </button>
      </div>

      <div v-if="modo === 'crear'" class="perfil-panel">
        <div v-if="pasoCrear === 'form'">
          <h3 class="panel-title">Paso 1: Ingresar datos</h3>
          <form @submit.prevent="validarYPrepararConfirmacion">
            <div class="form-group">
              <label>Nombre completo</label>
              <input type="text" v-model="nuevoPerfil.nombre" />
            </div>
            <div class="form-group">
              <label>Correo electrónico</label>
              <input type="email" v-model="nuevoPerfil.correo" />
            </div>
            <div class="form-group">
              <label>Contraseña</label>
              <input type="password" v-model="nuevoPerfil.clave" />
            </div>
            <div class="form-group">
              <label>Dirección</label>
              <input type="text" v-model="nuevoPerfil.direccion" />
            </div>
            <div class="form-group">
              <label>Teléfono</label>
              <input type="text" v-model="nuevoPerfil.telefono" />
            </div>
            <div class="form-group">
              <label>Rol</label>
              <select v-model="nuevoPerfil.rol">
                <option value="ADMIN">Administrador</option>
                <option value="CLIENTE">Cliente</option>
              </select>
            </div>

            <button type="submit" class="btn-ambos">
              Validar y continuar
            </button>
          </form>
        </div>

        <div v-else-if="pasoCrear === 'confirm'" class="perfil-confirm">
          <h3 class="panel-title">Confirmar datos</h3>
          <p><strong>Nombre:</strong> {{ perfilParaConfirmar.nombre }}</p>
          <p><strong>Correo:</strong> {{ perfilParaConfirmar.correo }}</p>
          <p><strong>Dirección:</strong> {{ perfilParaConfirmar.direccion }}</p>
          <p><strong>Teléfono:</strong> {{ perfilParaConfirmar.telefono }}</p>
          <p><strong>Rol:</strong> {{ perfilParaConfirmar.rol }}</p>

          <div class="perfil-confirm-buttons">
            <button class="btn-ambos" @click="confirmarYRegistrar">
              Confirmar y registrar
            </button>
            <button
              type="button"
              class="btn-secundario"
              @click="cancelarRegistro"
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>

      <div v-else class="perfil-panel">
        <h3 class="panel-title">Perfiles registrados</h3>

        <p v-if="loading">Cargando perfiles...</p>

        <table
          v-else
          class="perfil-table"
        >
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Correo</th>
              <th>Dirección</th>
              <th>Teléfono</th>
              <th>Rol</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="perfil in perfiles"
              :key="perfil.id"
              @click="seleccionarPerfil(perfil)"
              :class="{
                selected:
                  perfilSeleccionado && perfilSeleccionado.id === perfil.id,
              }"
            >
              <td>{{ perfil.nombre }}</td>
              <td>{{ perfil.correo }}</td>
              <td>{{ perfil.direccion }}</td>
              <td>{{ perfil.telefono }}</td>
              <td>{{ perfil.rol }}</td>
            </tr>
          </tbody>
        </table>

        <p v-if="!loading && !perfiles.length">
          No hay perfiles registrados aún.
        </p>

        <div
          v-if="perfilSeleccionado"
          class="perfil-detalle"
        >
          <h4>Ver perfil: {{ perfilSeleccionado.nombre }}</h4>
          <p><strong>Correo:</strong> {{ perfilSeleccionado.correo }}</p>
          <p><strong>Dirección:</strong> {{ perfilSeleccionado.direccion }}</p>
          <p><strong>Teléfono:</strong> {{ perfilSeleccionado.telefono }}</p>
          <p><strong>Rol:</strong> {{ perfilSeleccionado.rol }}</p>
        </div>
      </div>
    </div>

    <div v-else class="perfil-card">
      <header class="perfil-header">
        <h2>Tu perfil</h2>
      </header>

      <div class="perfil-detalle perfil-detalle-usuario">
        <p><strong>Nombre:</strong> {{ props.currentUser?.nombre }}</p>
        <p><strong>Correo:</strong> {{ props.currentUser?.correo }}</p>
        <p><strong>Dirección:</strong> {{ props.currentUser?.direccion }}</p>
        <p><strong>Teléfono:</strong> {{ props.currentUser?.telefono }}</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.perfil-wrapper {
  padding: 2.5rem 1rem 3rem;
  display: flex;
  justify-content: center;
}

.perfil-card {
  background: var(--cotton-light, #fcf5e9);
  border-radius: 20px;
  padding: 2rem 1.75rem;
  max-width: 900px;
  width: 100%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
}

.perfil-header {
  margin-bottom: 1.5rem;
  text-align: left;
}

.perfil-header h2 {
  font-size: 1.6rem;
  margin-bottom: 0.3rem;
  color: var(--cotton-dark, #1c262e);
}

.perfil-header p {
  color: #333;
  font-size: 0.95rem;
}

.perfil-alert {
  padding: 0.6rem 0.8rem;
  border-radius: 10px;
  font-size: 0.9rem;
  margin-bottom: 0.8rem;
}
.perfil-alert.error {
  background: #f8d7da;
  color: #842029;
}
.perfil-alert.ok {
  background: #d1e7dd;
  color: #0f5132;
}

.perfil-tabs {
  display: inline-flex;
  gap: 0.5rem;
  border-radius: 999px;
  background: #e6e6e6;
  padding: 0.2rem;
  margin-bottom: 1.5rem;
}
.tab-btn {
  border: none;
  background: transparent;
  padding: 0.5rem 1rem;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  color: #555;
  transition: background 0.15s ease, color 0.15s ease;
}
.tab-btn.active {
  background: #ffffff;
  color: var(--cotton-dark, #1c262e);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.perfil-panel {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.5rem;
  border: 1px solid #dddddd;
}
.panel-title {
  font-size: 1.1rem;
  margin-bottom: 0.75rem;
  color: var(--cotton-dark, #1c262e);
}

.perfil-confirm p {
  margin-bottom: 0.3rem;
  font-size: 0.9rem;
  color: #222;
}
.perfil-confirm-buttons {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}
.btn-secundario {
  display: inline-block;
  border: none;
  padding: 0.65rem 1.1rem;
  border-radius: 999px;
  background: #6c757d;
  color: #ffffff;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
}

.perfil-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
}
.perfil-table thead {
  background: #f0f0f0;
}
.perfil-table th,
.perfil-table td {
  padding: 0.55rem 0.6rem;
  border-bottom: 1px solid #e4e4e4;
  text-align: left;
  color: #222;
}
.perfil-table tr.selected {
  background: #e7f1ff;
}

.perfil-detalle {
  background: #ffffff;
  padding: 1.2rem;
  border-radius: 12px;
  border: 1px solid #ccc;
  font-size: 0.9rem;
  color: #222;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05);
}
.perfil-detalle h4 {
  margin-bottom: 0.6rem;
  color: #1c262e;
  font-weight: 700;
}
.perfil-detalle p {
  margin-bottom: 0.35rem;
}
.perfil-detalle strong {
  color: #000;
  font-weight: 600;
}
</style>

