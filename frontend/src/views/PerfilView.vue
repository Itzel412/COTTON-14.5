<script setup>
import { onMounted, ref, computed, watch } from 'vue';
import { getPerfiles, createPerfil, updatePerfil, deletePerfil, getMiPerfil } from '../data/api';

const emit = defineEmits(['perfil-updated', 'logout']);

const props = defineProps({
  currentUser: {
    type: Object,
    default: null,
  },
});

const esAdmin = computed(() => props.currentUser && props.currentUser.rol === 'ADMIN');

const modo = ref('crear');

const perfiles = ref([]);
const loading = ref(false);
const error = ref(null);
const mensaje = ref(null);

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
const miPerfil = ref(null);

const modal = ref({
  visible: false,
  tipo: '',
  titulo: '',
  mensaje: '',
  onConfirm: null,
});

const abrirConfirm = (titulo, mensajeTxt, cb) => {
  modal.value = { visible: true, tipo: 'confirm', titulo, mensaje: mensajeTxt, onConfirm: cb };
};
const abrirEdit = (titulo) => {
  modal.value = { visible: true, tipo: 'edit', titulo, mensaje: '', onConfirm: null };
};
const cerrarModal = () => { modal.value.visible = false; };

const editando = ref(null);
const formEditar = ref({
  nombre: '',
  correo: '',
  clave: '',
  direccion: '',
  telefono: '',
  rol: 'CLIENTE',
});

const cargarPerfilesAdmin = async () => {
  if (!esAdmin.value) return;
  loading.value = true;
  error.value = null;
  try {
    perfiles.value = await getPerfiles(props.currentUser);
  } catch (e) {
    error.value = e.message || 'Error al cargar perfiles';
  } finally {
    loading.value = false;
  }
};

const cargarMiPerfil = async () => {
  if (!props.currentUser) return;
  try {
    const p = await getMiPerfil(props.currentUser);
    miPerfil.value = { ...p };
  } catch {
    miPerfil.value = props.currentUser ? { ...props.currentUser } : null;
  }
};

const validarYPrepararConfirmacion = async () => {
  mensaje.value = null;
  error.value = null;

  const faltantes = [];
  if (!nuevoPerfil.value.nombre) faltantes.push('Nombre');
  if (!nuevoPerfil.value.correo) faltantes.push('Correo');
  if (!nuevoPerfil.value.clave) faltantes.push('Clave');
  if (!nuevoPerfil.value.direccion) faltantes.push('Dirección');
  if (!nuevoPerfil.value.telefono) faltantes.push('Teléfono');
  if (!nuevoPerfil.value.rol) faltantes.push('Rol');

  if (faltantes.length > 0) {
    error.value = `Campo faltante: ${faltantes.join(', ')}.`;
    return;
  }

  if (!perfiles.value.length) {
    await cargarPerfilesAdmin();
  }

  const correoRepetido = perfiles.value.some(
    (p) => p.correo && p.correo.toLowerCase() === nuevoPerfil.value.correo.toLowerCase(),
  );
  if (correoRepetido) {
    error.value = 'El correo electrónico ya está en uso';
    return;
  }

  perfilParaConfirmar.value = { ...nuevoPerfil.value };
  pasoCrear.value = 'confirm';
  mensaje.value = 'Verifica que estos sean los datos correctos antes de registrar al usuario.';
};

const confirmarYRegistrar = async () => {
  if (!perfilParaConfirmar.value) return;

  try {
    error.value = null;
    mensaje.value = null;

    perfiles.value = await createPerfil({ ...perfilParaConfirmar.value }, props.currentUser);

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
    mensaje.value = 'Usuario registrado exitosamente.';
  } catch (e) {
    error.value = e.message || 'Error al registrar el usuario';
  }
};

const cancelarRegistro = () => {
  perfilParaConfirmar.value = null;
  pasoCrear.value = 'form';
  mensaje.value = 'Se canceló el registro. No se guardaron los datos.';
};

const abrirEditarPerfilAdmin = (p) => {
  editando.value = { ...p };
  formEditar.value = {
    nombre: p.nombre || '',
    correo: p.correo || '',
    clave: p.clave || '',       
    direccion: p.direccion || '',
    telefono: p.telefono || '',
    rol: p.rol || 'CLIENTE',
  };
  abrirEdit(`Editar perfil: ${p.nombre}`);
};

const guardarEdicionAdmin = async () => {
  if (!editando.value) return;

  error.value = null;
  mensaje.value = null;

  const faltantes = [];
  if (!formEditar.value.nombre?.trim()) faltantes.push('Nombre');
  if (!formEditar.value.correo?.trim()) faltantes.push('Correo');
  if (!formEditar.value.clave?.trim()) faltantes.push('Contraseña');
  if (!formEditar.value.direccion?.trim()) faltantes.push('Dirección');
  if (!formEditar.value.telefono?.trim()) faltantes.push('Teléfono');
  if (!formEditar.value.rol?.trim()) faltantes.push('Rol');

  if (faltantes.length) {
    error.value = `Campo(s) faltante(s): ${faltantes.join(', ')}.`;
    return;
  }

  try {
    await updatePerfil(editando.value.id, { ...formEditar.value }, props.currentUser);
    cerrarModal();
    mensaje.value = 'Perfil actualizado correctamente.';
    await cargarPerfilesAdmin();
  } catch (e) {
    error.value = e.message || 'Error al actualizar el perfil.';
  }
};

const solicitarEliminarAdmin = (p) => {
  abrirConfirm(
    'Eliminar perfil',
    `¿Seguro que deseas eliminar el perfil de ${p.nombre} (${p.correo})?`,
    async () => {
      cerrarModal();
      error.value = null;
      mensaje.value = null;

      try {
        await deletePerfil(p.id, props.currentUser);
        mensaje.value = 'Perfil eliminado correctamente.';
        await cargarPerfilesAdmin();
      } catch (e) {
        error.value = e.message || 'Error al eliminar el perfil.';
      }
    }
  );
};

const abrirEditarMiPerfil = () => {
  if (!miPerfil.value) return;
  editando.value = { ...miPerfil.value };
  formEditar.value = {
    nombre: miPerfil.value.nombre || '',
    correo: miPerfil.value.correo || '',
    clave: miPerfil.value.clave || '', 
    direccion: miPerfil.value.direccion || '',
    telefono: miPerfil.value.telefono || '',
    rol: 'CLIENTE',
  };
  abrirEdit('Editar tu perfil');
};

const guardarEdicionCliente = async () => {
  if (!miPerfil.value) return;

  error.value = null;
  mensaje.value = null;

  const faltantes = [];
  if (!formEditar.value.nombre?.trim()) faltantes.push('Nombre');
  if (!formEditar.value.correo?.trim()) faltantes.push('Correo');
  if (!formEditar.value.clave?.trim()) faltantes.push('Contraseña');
  if (!formEditar.value.direccion?.trim()) faltantes.push('Dirección');
  if (!formEditar.value.telefono?.trim()) faltantes.push('Teléfono');

  if (faltantes.length) {
    error.value = `Campo(s) faltante(s): ${faltantes.join(', ')}.`;
    return;
  }

  try {
    await updatePerfil(
      miPerfil.value.id,
      { ...formEditar.value, rol: 'CLIENTE' },
      props.currentUser
    );

    cerrarModal();
    mensaje.value = 'Tu perfil fue actualizado correctamente.';

    await cargarMiPerfil();
    emit('perfil-updated', { ...miPerfil.value });
  } catch (e) {
    error.value = e.message || 'Error al actualizar tu perfil.';
  }
};

const solicitarEliminarMiPerfil = () => {
  if (!miPerfil.value) return;

  abrirConfirm(
    'Eliminar cuenta',
    '¿Seguro que deseas eliminar tu cuenta? Esta acción no se puede deshacer.',
    async () => {
      cerrarModal();
      error.value = null;
      mensaje.value = null;

      try {
        await deletePerfil(miPerfil.value.id, props.currentUser);
        mensaje.value = 'Tu cuenta fue eliminada.';
        emit('logout');
      } catch (e) {
        error.value = e.message || 'Error al eliminar tu cuenta.';
      }
    }
  );
};

onMounted(async () => {
  if (esAdmin.value) {
    await cargarPerfilesAdmin();
  } else {
    await cargarMiPerfil();
  }
});

watch(
  () => props.currentUser,
  async () => {
    error.value = null;
    mensaje.value = null;
    perfiles.value = [];
    miPerfil.value = null;

    if (esAdmin.value) {
      await cargarPerfilesAdmin();
    } else {
      await cargarMiPerfil();
    }
  }
);
</script>

<template>
  <section class="perfil-wrapper">
    <div class="perfil-card">
      <header class="perfil-header">
        <h2 v-if="esAdmin">Perfiles</h2>
        <h2 v-else>Tu perfil</h2>
        <p v-if="esAdmin">Aquí puedes crear, ver, editar o eliminar perfiles.</p>
      </header>

      <transition name="fade">
        <div v-if="modal.visible" class="alert-overlay">
          <div class="alert-box">
            <p class="alert-text"><strong>{{ modal.titulo }}</strong></p>

            <template v-if="modal.tipo === 'confirm'">
              <p class="alert-text">{{ modal.mensaje }}</p>
              <div class="confirm-actions">
                <button type="button" class="alert-btn secondary" @click="cerrarModal">Cancelar</button>
                <button type="button" class="alert-btn" @click="() => { if (modal.onConfirm) modal.onConfirm(); }">
                  Confirmar
                </button>
              </div>
            </template>

            <template v-else>
              <div class="edit-form">
                <div class="form-group">
                  <label>Nombre completo</label>
                  <input type="text" v-model="formEditar.nombre" />
                </div>

                <div class="form-group">
                  <label>Correo electrónico</label>
                  <input type="email" v-model="formEditar.correo" />
                </div>

                <div class="form-group">
                  <label>Contraseña</label>
                  <input type="text" v-model="formEditar.clave" />
                </div>

                <div class="form-group">
                  <label>Dirección</label>
                  <input type="text" v-model="formEditar.direccion" />
                </div>

                <div class="form-group">
                  <label>Teléfono</label>
                  <input type="text" v-model="formEditar.telefono" />
                </div>

                <div class="form-group" v-if="esAdmin">
                  <label>Rol</label>
                  <select v-model="formEditar.rol">
                    <option value="ADMIN">Administrador</option>
                    <option value="CLIENTE">Cliente</option>
                  </select>
                </div>

                <div class="confirm-actions">
                  <button type="button" class="alert-btn secondary" @click="cerrarModal">Cancelar</button>
                  <button
                    type="button"
                    class="alert-btn"
                    @click="esAdmin ? guardarEdicionAdmin() : guardarEdicionCliente()"
                  >
                    Guardar
                  </button>
                </div>
              </div>
            </template>
          </div>
        </div>
      </transition>

      <transition name="fade">
        <div v-if="(error || mensaje) && !modal.visible" class="alert-overlay">
          <div class="alert-box" :class="{ 'alert-error': error, 'alert-success': mensaje }">
            <p class="alert-text">{{ error || mensaje }}</p>
            <button type="button" class="alert-btn" @click="() => { error = null; mensaje = null; }">
              OK
            </button>
          </div>
        </div>
      </transition>

      <template v-if="esAdmin">
        <div class="perfil-tabs">
          <button type="button" class="tab-btn" :class="{ active: modo === 'crear' }" @click="modo = 'crear'">
            Crear perfil
          </button>
          <button type="button" class="tab-btn" :class="{ active: modo === 'ver' }" @click="modo = 'ver'">
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
                <input type="text" v-model="nuevoPerfil.clave" />
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

          <div v-else class="perfil-confirm">
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
              <button type="button" class="btn-secundario" @click="cancelarRegistro">
                Cancelar
              </button>
            </div>
          </div>
        </div>

        <div v-else class="perfil-panel">
          <h3 class="panel-title">Perfiles registrados</h3>
          <p v-if="loading">Cargando perfiles...</p>

          <div v-else class="perfil-table-wrapper">
            <table class="perfil-table">
              <thead>
                <tr>
                  <th class="col-nombre">Nombre</th>
                  <th class="col-correo">Correo</th>
                  <th class="col-direccion">Dirección</th>
                  <th class="col-telefono">Teléfono</th>
                  <th class="col-rol">Rol</th>
                  <th class="col-acciones"></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in perfiles" :key="p.id">
                  <td class="col-nombre">{{ p.nombre }}</td>
                  <td class="col-correo" :title="p.correo">{{ p.correo }}</td>
                  <td class="col-direccion" :title="p.direccion">{{ p.direccion }}</td>
                  <td class="col-telefono">{{ p.telefono }}</td>
                  <td class="col-rol">{{ p.rol }}</td>

                  <td class="acciones">
                    <button
                      type="button"
                      class="icon-btn"
                      title="Editar"
                      aria-label="Editar"
                      @click="abrirEditarPerfilAdmin(p)"
                    >
                      <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                        <path
                          d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25Zm2.92 2.83H5v-.92l9.06-9.06.92.92-9.06 9.06ZM20.71 7.04a1 1 0 0 0 0-1.41L18.37 3.29a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83Z"
                        />
                      </svg>
                    </button>

                    <button
                      type="button"
                      class="icon-btn danger"
                      title="Eliminar"
                      aria-label="Eliminar"
                      @click="solicitarEliminarAdmin(p)"
                    >
                      <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                        <path
                          d="M9 3h6l1 2h4v2H4V5h4l1-2Zm1 7h2v9h-2v-9Zm4 0h2v9h-2v-9ZM7 10h2v9H7v-9Zm-1 12h12a2 2 0 0 0 2-2V9H4v11a2 2 0 0 0 2 2Z"
                        />
                      </svg>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>

            <p v-if="!perfiles.length" class="empty-text">
              No hay perfiles registrados aún.
            </p>
          </div>
        </div>
      </template>

      <template v-else>
        <div class="perfil-panel">
          <div v-if="!miPerfil" class="empty-text">Cargando tu perfil...</div>

          <div v-else class="perfil-detalle perfil-detalle-usuario">
            <p><strong>Nombre:</strong> {{ miPerfil.nombre }}</p>
            <p><strong>Correo:</strong> {{ miPerfil.correo }}</p>
            <p><strong>Dirección:</strong> {{ miPerfil.direccion }}</p>
            <p><strong>Teléfono:</strong> {{ miPerfil.telefono }}</p>

            <div class="acciones-cliente">
              <button type="button" class="icon-btn" title="Editar" aria-label="Editar" @click="abrirEditarMiPerfil">
                <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                  <path
                    d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25Zm2.92 2.83H5v-.92l9.06-9.06.92.92-9.06 9.06ZM20.71 7.04a1 1 0 0 0 0-1.41L18.37 3.29a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83Z"
                  />
                </svg>
              </button>

              <button type="button" class="icon-btn danger" title="Eliminar" aria-label="Eliminar" @click="solicitarEliminarMiPerfil">
                <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                  <path
                    d="M9 3h6l1 2h4v2H4V5h4l1-2Zm1 7h2v9h-2v-9Zm4 0h2v9h-2v-9ZM7 10h2v9H7v-9Zm-1 12h12a2 2 0 0 0 2-2V9H4v11a2 2 0 0 0 2 2Z"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </template>
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
  max-width: 1000px;
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
  font-weight: 700;
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

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.6rem 0.75rem;
  border-radius: 10px;
  border: 1px solid #d0d4d7;
  font-size: 0.9rem;
  outline: none;
  background-color: #fafafa;
}

.btn-ambos {
  width: 100%;
  border: none;
  border-radius: 999px;
  padding: 0.8rem 1.2rem;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  background: #e18b6b;
  color: #ffffff;
  transition: background 0.15s ease, transform 0.08s ease;
}

.btn-ambos:hover {
  background: #d67a5d;
  transform: translateY(-1px);
}

.btn-secundario {
  border: none;
  padding: 0.75rem 1.1rem;
  border-radius: 999px;
  background: #6c757d;
  color: #ffffff;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 700;
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

.perfil-table-wrapper {
  width: 100%;
}

.perfil-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  margin-top: 0.5rem;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  table-layout: fixed;
}

.perfil-table thead {
  background: #f0f0f0;
}

.perfil-table th,
.perfil-table td {
  padding: 0.55rem 0.9rem; 
  border-bottom: 1px solid #e4e4e4;
  text-align: left;
  color: #222;
  vertical-align: top;
}

.col-nombre { width: 22%; }
.col-correo { width: 22%; }
.col-direccion { width: 22%; }
.col-telefono { width: 16%; }
.col-rol { width: 10%; }

.col-acciones {
  width: 120px;
}

.acciones {
  text-align: right;
  white-space: nowrap;
}

.perfil-table td.col-correo,
.perfil-table td.col-direccion {
   white-space: normal;
   overflow-wrap: anywhere;
   word-break: break-word;
}

.empty-text {
  margin-top: 0.75rem;
  color: #555;
}

.icon-btn {
  border: none;
  background: #f2f2f2;
  border-radius: 10px;
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.12s ease, background 0.12s ease;
  margin-left: 0.4rem;
}

.icon-btn svg {
  fill: #1c262e;
}

.icon-btn:hover {
  transform: translateY(-1px);
  background: #e9e9e9;
}

.icon-btn.danger {
  background: #c8421a;
}

.icon-btn.danger svg {
  fill: #ffffff;
}

.icon-btn.danger:hover {
  background: #b43a16;
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

.perfil-detalle p {
  margin-bottom: 0.35rem;
}

.acciones-cliente {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.75rem;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.alert-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 30;
}

.alert-box {
  background: #ffffff;
  padding: 1.2rem 1.5rem;
  border-radius: 14px;
  max-width: 520px;
  width: 92%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
  text-align: center;
}

.alert-text {
  margin-bottom: 0.9rem;
  font-size: 0.95rem;
}

.alert-error {
  color: #b02a37;
}

.alert-success {
  color: #0f5132;
}

.alert-btn {
  border: none;
  padding: 0.6rem 1.4rem;
  border-radius: 999px;
  background: var(--cotton-accent, #e18b6b);
  color: #ffffff;
  font-weight: 800;
  cursor: pointer;
}

.alert-btn.secondary {
  background: #6c757d;
}

.confirm-actions {
  display: flex;
  gap: 0.6rem;
  justify-content: center;
  margin-top: 0.2rem;
}

.edit-form {
  margin-top: 0.5rem;
  text-align: left;
}
</style>
