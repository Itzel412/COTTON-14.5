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
  <section>
    <template v-if="esAdmin">
      <h2 style="margin-bottom: 0.5rem;">Gestión de Perfiles (Admin)</h2>
      <p style="margin-bottom: 1rem;">
        El administrador puede crear perfiles y consultar todos los usuarios.
      </p>

      <div
        v-if="error"
        style="margin-bottom: 1rem; color: #c1121f; font-size: 0.9rem;"
      >
        {{ error }}
      </div>
      <div
        v-if="mensajeCrear"
        style="margin-bottom: 1rem; color: #1b4965; font-size: 0.9rem;"
      >
        {{ mensajeCrear }}
      </div>

      <div
        style="
          display: flex;
          gap: 0.5rem;
          margin-bottom: 1rem;
          border-bottom: 1px solid #ddd;
          padding-bottom: 0.5rem;
        "
      >
        <button
          class="btn-ambos"
          :style="{ background: modo === 'crear' ? '#1b4965' : '#ccc' }"
          @click="modo = 'crear'"
        >
          Crear perfil
        </button>
        <button
          class="btn-ambos"
          :style="{ background: modo === 'ver' ? '#1b4965' : '#ccc' }"
          @click="modo = 'ver'"
        >
          Ver perfiles
        </button>
      </div>

      <div v-if="modo === 'crear'">
        <div
          style="
            background: #fff;
            padding: 1rem;
            border-radius: 12px;
            border: 1px solid #ddd;
          "
        >
          <div v-if="pasoCrear === 'form'">
            <h3 style="margin-bottom: 0.75rem;">Paso 1: Ingresar datos</h3>
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

          <div v-else-if="pasoCrear === 'confirm'">
            <h3>Confirmar datos</h3>
            <p><strong>Nombre:</strong> {{ perfilParaConfirmar.nombre }}</p>
            <p><strong>Correo:</strong> {{ perfilParaConfirmar.correo }}</p>
            <p><strong>Dirección:</strong> {{ perfilParaConfirmar.direccion }}</p>
            <p><strong>Teléfono:</strong> {{ perfilParaConfirmar.telefono }}</p>
            <p><strong>Rol:</strong> {{ perfilParaConfirmar.rol }}</p>
            <div style="display: flex; gap: 0.5rem; margin-top: 1rem;">
              <button class="btn-ambos" @click="confirmarYRegistrar">
                Confirmar y registrar
              </button>
              <button
                class="btn-ambos"
                style="background: #6c757d;"
                @click="cancelarRegistro"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-else>
        <div
          style="
            background: #fff;
            padding: 1rem;
            border-radius: 12px;
            border: 1px solid #ddd;
          "
        >
          <h3 style="margin-bottom: 0.75rem;">Perfiles registrados</h3>

          <p v-if="loading">Cargando perfiles...</p>

          <table
            v-else
            style="
              width: 100%;
              border-collapse: collapse;
              font-size: 0.9rem;
              margin-bottom: 1rem;
            "
          >
            <thead>
              <tr>
                <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
                  Nombre
                </th>
                <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
                  Correo
                </th>
                <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
                  Dirección
                </th>
                <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
                  Teléfono
                </th>
                <th style="border-bottom: 1px solid #ddd; padding: 0.5rem;">
                  Rol
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="perfil in perfiles"
                :key="perfil.id"
                @click="seleccionarPerfil(perfil)"
                :style="{
                  borderBottom: '1px solid #eee',
                  background:
                    perfilSeleccionado && perfilSeleccionado.id === perfil.id
                      ? '#e7f1ff'
                      : 'transparent',
                  cursor: 'pointer',
                }"
              >
                <td style="padding: 0.5rem;">{{ perfil.nombre }}</td>
                <td style="padding: 0.5rem;">{{ perfil.correo }}</td>
                <td style="padding: 0.5rem;">{{ perfil.direccion }}</td>
                <td style="padding: 0.5rem;">{{ perfil.telefono }}</td>
                <td style="padding: 0.5rem;">{{ perfil.rol }}</td>
              </tr>
            </tbody>
          </table>

          <div
            v-if="perfilSeleccionado"
            style="
              background: #f8f9fa;
              padding: 1rem;
              border-radius: 12px;
              border: 1px solid #ddd;
              font-size: 0.9rem;
            "
          >
            <h4>Ver perfil: {{ perfilSeleccionado.nombre }}</h4>
            <p><strong>ID:</strong> {{ perfilSeleccionado.id }}</p>
            <p><strong>Correo:</strong> {{ perfilSeleccionado.correo }}</p>
            <p><strong>Dirección:</strong> {{ perfilSeleccionado.direccion }}</p>
            <p><strong>Teléfono:</strong> {{ perfilSeleccionado.telefono }}</p>
            <p><strong>Rol:</strong> {{ perfilSeleccionado.rol }}</p>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <h2 style="margin-bottom: 0.5rem;">Mi perfil</h2>
      <p style="margin-bottom: 1rem;">
        Aquí puedes consultar los datos de tu cuenta.
      </p>

      <div
        style="
          background: #fff;
          padding: 1rem;
          border-radius: 12px;
          border: 1px solid #ddd;
          font-size: 0.9rem;
        "
      >
        <p><strong>ID:</strong> {{ props.currentUser?.id }}</p>
        <p><strong>Nombre:</strong> {{ props.currentUser?.nombre }}</p>
        <p><strong>Correo:</strong> {{ props.currentUser?.correo }}</p>
        <p><strong>Dirección:</strong> {{ props.currentUser?.direccion }}</p>
        <p><strong>Teléfono:</strong> {{ props.currentUser?.telefono }}</p>
        <p><strong>Rol:</strong> {{ props.currentUser?.rol }}</p>
      </div>
    </template>
  </section>
</template>
