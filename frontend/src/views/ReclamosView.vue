<script setup>
import { ref, computed, onMounted } from 'vue';
import {
  getReclamos,
  createReclamo,
  updateReclamoEstado,
  deleteReclamo
} from '../data/api';

const props = defineProps({
  currentUser: { type: Object, required: true },
});

const esAdmin = computed(() => props.currentUser?.rol === 'ADMIN');

const reclamos = ref([]);
const loading = ref(false);
const error = ref(null);
const mensaje = ref(null);

const modo = ref('crear');

const pasoCrear = ref('form');
const nuevoReclamo = ref({
  titulo: '',
  descripcion: '',
});

const reclamoParaConfirmar = ref(null);

const modal = ref({
  visible: false,
  titulo: '',
  mensaje: '',
  onConfirm: null,
});
const abrirConfirm = (titulo, mensajeTxt, cb) => {
  modal.value = { visible: true, titulo, mensaje: mensajeTxt, onConfirm: cb };
};
const cerrarConfirm = () => { modal.value.visible = false; };

const ESTADOS = [
  { value: 'PENDIENTE', label: 'Pendiente' },
  { value: 'EN_PROCESO', label: 'En proceso' },
  { value: 'CERRADO', label: 'Cerrado' },
];

const reclamosFiltrados = computed(() => {
  if (esAdmin.value) return reclamos.value;
  const correo = (props.currentUser?.correo || '').trim();
  return reclamos.value.filter((r) => (r.usuario || '').trim() === correo);
});

const cargarReclamos = async () => {
  loading.value = true;
  error.value = null;
  try {
    reclamos.value = await getReclamos(props.currentUser);
  } catch (e) {
    error.value = e.message || 'Error al cargar reclamos';
  } finally {
    loading.value = false;
  }
};

const validarYConfirmar = () => {
  error.value = null;
  mensaje.value = null;

  const faltantes = [];
  if (!nuevoReclamo.value.titulo?.trim()) faltantes.push('Título');
  if (!nuevoReclamo.value.descripcion?.trim()) faltantes.push('Descripción');

  if (faltantes.length) {
    error.value = `Campo(s) faltante(s): ${faltantes.join(', ')}.`;
    return;
  }

  if (nuevoReclamo.value.descripcion.trim().length < 50) {
    error.value = 'La descripción debe tener al menos 50 caracteres.';
    return;
  }

  reclamoParaConfirmar.value = {
    titulo: nuevoReclamo.value.titulo.trim(),
    descripcion: nuevoReclamo.value.descripcion.trim(),
  };
  pasoCrear.value = 'confirm';
};

const confirmarCreacion = async () => {
  if (!reclamoParaConfirmar.value) return;

  error.value = null;
  mensaje.value = null;

  const payload = {
    usuario: props.currentUser.correo,
    titulo: reclamoParaConfirmar.value.titulo,
    descripcion: reclamoParaConfirmar.value.descripcion,
  };

  try {
    const ok = await createReclamo(payload, props.currentUser);
    if (!ok) {
      error.value = 'El backend no pudo registrar el reclamo.';
      return;
    }

    mensaje.value = 'Reclamo creado exitosamente.';
    nuevoReclamo.value = { titulo: '', descripcion: '' };
    reclamoParaConfirmar.value = null;
    pasoCrear.value = 'form';
    await cargarReclamos();
  } catch (e) {
    error.value = e.message || 'Error al registrar el reclamo.';
  }
};

const cancelarCreacion = () => {
  reclamoParaConfirmar.value = null;
  pasoCrear.value = 'form';
};

const cambiarEstado = async (reclamo, nuevoEstado) => {
  if (!esAdmin.value) return;
  if (!nuevoEstado || nuevoEstado === reclamo.estado) return;

  error.value = null;
  mensaje.value = null;

  try {
    const ok = await updateReclamoEstado(reclamo.id, nuevoEstado, props.currentUser);
    if (!ok) {
      error.value = 'No se pudo actualizar el estado del reclamo.';
      return;
    }
    reclamo.estado = nuevoEstado;
    mensaje.value = 'Estado del reclamo actualizado.';
  } catch (e) {
    error.value = e.message || 'Error al actualizar el estado.';
  }
};

const solicitarEliminar = (reclamo) => {
  if (!esAdmin.value) return;

  abrirConfirm(
    'Eliminar reclamo',
    `¿Seguro que deseas eliminar el reclamo #${reclamo.id} (${reclamo.titulo})?`,
    async () => {
      cerrarConfirm();
      error.value = null;
      mensaje.value = null;

      try {
        const ok = await deleteReclamo(reclamo.id, props.currentUser);
        if (!ok) {
          error.value = 'No se pudo eliminar el reclamo.';
          return;
        }
        mensaje.value = 'Reclamo eliminado correctamente.';
        await cargarReclamos();
      } catch (e) {
        error.value = e.message || 'Error al eliminar el reclamo.';
      }
    }
  );
};

onMounted(() => {
  cargarReclamos();
});
</script>

<template>
  <section class="reclamos-wrapper">
    <div class="reclamos-card">
      <header class="reclamos-header">
        <h2 v-if="esAdmin">Reclamos</h2>
        <h2 v-else>Atención al cliente</h2>

        <p v-if="esAdmin">
          Revisa, actualiza o elimina los reclamos de clientes.
        </p>
        <p v-else>
          Crea reclamos sobre tus pedidos o productos para que el administrador revise tu caso.
        </p>
      </header>

      <transition name="fade">
        <div v-if="modal.visible" class="alert-overlay">
          <div class="alert-box">
            <p class="alert-text"><strong>{{ modal.titulo }}</strong></p>
            <p class="alert-text">{{ modal.mensaje }}</p>
            <div class="confirm-actions">
              <button type="button" class="alert-btn secondary" @click="cerrarConfirm">Cancelar</button>
              <button type="button" class="alert-btn" @click="() => { if (modal.onConfirm) modal.onConfirm(); }">
                Eliminar
              </button>
            </div>
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
        <div class="rec-panel">
          <h3 class="panel-title">Reclamos registrados</h3>

          <p v-if="loading">Cargando reclamos...</p>

          <div v-else class="rec-table-wrapper">
            <table class="rec-table rec-table-admin">
              <thead>
                <tr>
                  <th>ID</th>
                  <th class="col-cliente">Cliente</th>
                  <th class="col-titulo">Título</th>
                  <th>Descripción</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                  <th class="col-acciones"></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in reclamosFiltrados" :key="r.id">
                  <td>{{ r.id }}</td>

                  <td class="col-cliente" :title="r.usuario">{{ r.usuario }}</td>
                  <td class="col-titulo" :title="r.titulo">{{ r.titulo }}</td>

                  <td class="col-desc">{{ r.descripcion }}</td>
                  <td>{{ r.fechaCreacion }}</td>
                  <td>
                    <select :value="r.estado" @change="(ev) => cambiarEstado(r, ev.target.value)">
                      <option v-for="opt in ESTADOS" :key="opt.value" :value="opt.value">
                        {{ opt.label }}
                      </option>
                    </select>
                  </td>

                  <td class="acciones">
                    <button
                      type="button"
                      class="icon-btn danger"
                      title="Eliminar reclamo"
                      aria-label="Eliminar reclamo"
                      @click="solicitarEliminar(r)"
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

            <p v-if="!loading && !reclamosFiltrados.length" class="rec-empty-text">
              No hay reclamos registrados.
            </p>
          </div>
        </div>
      </template>

      <template v-else>
        <div class="rec-tabs">
          <button type="button" class="rec-tab-btn" :class="{ active: modo === 'crear' }" @click="modo = 'crear'">
            Crear reclamo
          </button>
          <button type="button" class="rec-tab-btn" :class="{ active: modo === 'mis-reclamos' }" @click="modo = 'mis-reclamos'">
            Mis reclamos
          </button>
        </div>

        <div v-if="modo === 'crear'" class="rec-panel">
          <h3 class="panel-title">Nuevo reclamo</h3>

          <div v-if="pasoCrear === 'form'">
            <form @submit.prevent="validarYConfirmar">
              <div class="form-group">
                <label>Título</label>
                <input type="text" v-model="nuevoReclamo.titulo" />
              </div>

              <div class="form-group">
                <label>Descripción (mínimo 50 caracteres)</label>
                <textarea rows="4" v-model="nuevoReclamo.descripcion"></textarea>
              </div>

              <button type="submit" class="btn-ambos">Validar y continuar</button>
            </form>
          </div>

          <div v-else class="reclamo-confirm">
            <h4>Confirmar datos del reclamo</h4>
            <p class="reclamo-confirm-text">
              <strong>Título:</strong> {{ reclamoParaConfirmar.titulo }}
            </p>
            <p class="reclamo-confirm-text">
              <strong>Descripción:</strong> {{ reclamoParaConfirmar.descripcion }}
            </p>

            <div class="reclamo-confirm-buttons">
              <button type="button" class="btn-ambos" @click="confirmarCreacion">
                Confirmar y enviar
              </button>
              <button type="button" class="btn-ambos cancelar" @click="cancelarCreacion">
                Cancelar
              </button>
            </div>
          </div>
        </div>

        <div v-else class="rec-panel">
          <h3 class="panel-title">Mis reclamos</h3>

          <p v-if="loading">Cargando reclamos...</p>

          <div v-else class="rec-table-wrapper">
            <table class="rec-table cliente">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Título</th>
                  <th>Descripción</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in reclamosFiltrados" :key="r.id">
                  <td>{{ r.id }}</td>
                  <td>{{ r.titulo }}</td>
                  <td class="col-desc">{{ r.descripcion }}</td>
                  <td>{{ r.fechaCreacion }}</td>
                  <td>{{ r.estado }}</td>
                </tr>
              </tbody>
            </table>

            <p v-if="!loading && !reclamosFiltrados.length" class="rec-empty-text">
              Todavía no has registrado reclamos.
            </p>
          </div>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.reclamos-wrapper {
  padding: 2.5rem 1rem 3rem;
  display: flex;
  justify-content: center;
}

.reclamos-card {
  background: var(--cotton-light, #fcf5e9);
  border-radius: 20px;
  padding: 2rem 1.75rem;
  max-width: 1300px;
  width: 100%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
}

.reclamos-header {
  margin-bottom: 1.5rem;
}

.reclamos-header h2 {
  font-size: 1.6rem;
  margin-bottom: 0.3rem;
  color: var(--cotton-dark, #1c262e);
}

.reclamos-header p {
  color: #555;
  font-size: 0.95rem;
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
  max-width: 460px;
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
  font-weight: 700;
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

.rec-tabs {
  display: inline-flex;
  gap: 0.5rem;
  border-radius: 999px;
  background: #e6e6e6;
  padding: 0.2rem;
  margin-bottom: 1.5rem;
}

.rec-tab-btn {
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

.rec-tab-btn.active {
  background: #ffffff;
  color: var(--cotton-dark, #1c262e);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.rec-panel {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.5rem 1.75rem;
  border: 1px solid #dddddd;
}

.panel-title {
  font-size: 1.1rem;
  margin-bottom: 0.75rem;
  color: var(--cotton-dark, #1c262e);
}

.rec-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  table-layout: fixed;
}

.rec-table thead {
  background: #f0f0f0;
}

.rec-table th,
.rec-table td {
  padding: 0.55rem 0.7rem;
  border-bottom: 1px solid #e4e4e4;
  text-align: left;
}

.rec-table th {
  color: #1c262e;
  font-weight: 700;
}
.rec-table td {
  color: #333333;
}

.col-desc {
  word-break: break-word;
  white-space: normal;
}

.rec-table-admin th.col-cliente,
.rec-table-admin td.col-cliente {
  width: 18%;
  padding-right: 1.2rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rec-table-admin th.col-titulo,
.rec-table-admin td.col-titulo {
  width: 16%;
  padding-left: 1.0rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-acciones {
  width: 10%;
}

.acciones {
  text-align: right;
}

.rec-table td:last-child select {
  width: 100%;
  min-width: 0;
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

.reclamo-confirm {
  padding: 1rem;
  border-radius: 12px;
  border: 1px solid #e4e4e4;
  background: #faf4ec;
  color: #1b1b1b;
}

.reclamo-confirm-text {
  font-size: 0.95rem;
  margin-bottom: 0.4rem;
}

.reclamo-confirm-buttons {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.reclamo-confirm-buttons .cancelar {
  background: #6c757d;
  box-shadow: none;
}
</style>
