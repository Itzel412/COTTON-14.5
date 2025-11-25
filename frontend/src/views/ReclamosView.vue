<script setup>
import { ref, computed, onMounted } from 'vue';
import { getReclamos, createReclamo, updateReclamoEstado } from '../data/api';

const props = defineProps({
  currentUser: {
    type: Object,
    required: true,
  },
});

const esAdmin = computed(() => props.currentUser?.rol === 'ADMIN');

const reclamos = ref([]);
const loading = ref(false);
const error = ref(null);
const mensaje = ref(null);

const modo = ref('crear'); // cliente: crear / mis-reclamos

const pasoCrear = ref('form');
const nuevoReclamo = ref({
  titulo: '',
  descripcion: '',
});

const reclamoParaConfirmar = ref(null);

const ESTADOS = [
  { value: 'PENDIENTE', label: 'Pendiente' },
  { value: 'EN_PROCESO', label: 'En proceso' },
  { value: 'CERRADO', label: 'Cerrado' },
];

const reclamosFiltrados = computed(() => {
  if (esAdmin.value) return reclamos.value;
  const correo = props.currentUser?.correo || '';
  return reclamos.value.filter((r) => r.usuario === correo);
});

const cargarReclamos = async () => {
  loading.value = true;
  error.value = null;
  try {
    reclamos.value = await getReclamos();
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
  if (!nuevoReclamo.value.titulo) faltantes.push('Título');
  if (!nuevoReclamo.value.descripcion) faltantes.push('Descripción');

  if (faltantes.length) {
    error.value = `Campo(s) faltante(s): ${faltantes.join(', ')}.`;
    return;
  }

  if (nuevoReclamo.value.descripcion.trim().length < 50) {
    error.value = 'La descripción debe tener al menos 50 caracteres.';
    return;
  }

  reclamoParaConfirmar.value = {
    titulo: nuevoReclamo.value.titulo,
    descripcion: nuevoReclamo.value.descripcion,
  };
  pasoCrear.value = 'confirm';
};

const confirmarCreacion = async () => {
  if (!reclamoParaConfirmar.value) return;

  error.value = null;

  const payload = {
    usuario: props.currentUser.correo,
    titulo: reclamoParaConfirmar.value.titulo,
    descripcion: reclamoParaConfirmar.value.descripcion,
  };

  try {
    const ok = await createReclamo(payload);
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
  if (!nuevoEstado || nuevoEstado === reclamo.estado) return;

  error.value = null;
  mensaje.value = null;
  try {
    const ok = await updateReclamoEstado(reclamo.id, nuevoEstado);
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

onMounted(() => {
  cargarReclamos();
});
</script>

<template>
  <section class="reclamos-wrapper">
    <div class="reclamos-card">
      <header class="reclamos-header">
        <h2 v-if="esAdmin">Gestión de reclamos</h2>
        <h2 v-else>Atención al cliente</h2>

        <p v-if="esAdmin">
          Visualiza los reclamos de los clientes y actualiza su estado.
        </p>
        <p v-else>
          Crea reclamos sobre tus pedidos o productos para que el
          administrador pueda revisar tu caso.
        </p>
      </header>

      <transition name="fade">
        <div v-if="error || mensaje" class="alert-overlay">
          <div
            class="alert-box"
            :class="{ 'alert-error': error, 'alert-success': mensaje }"
          >
            <p class="alert-text">
              {{ error || mensaje }}
            </p>
            <button
              type="button"
              class="alert-btn"
              @click="() => { error = null; mensaje = null; }"
            >
              OK
            </button>
          </div>
        </div>
      </transition>

      <!-- ADMIN -->
      <template v-if="esAdmin">
        <div class="rec-panel">
          <h3 class="panel-title">Reclamos registrados</h3>

          <p v-if="loading">Cargando reclamos...</p>

          <div v-else class="rec-table-wrapper">
            <table class="rec-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Cliente</th>
                  <th>Título</th>
                  <th>Descripción</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in reclamosFiltrados" :key="r.id">
                  <td>{{ r.id }}</td>
                  <td>{{ r.usuario }}</td>
                  <td>{{ r.titulo }}</td>
                  <td>{{ r.descripcion }}</td>
                  <td>{{ r.fechaCreacion }}</td>
                  <td>
                    <select
                      :value="r.estado"
                      @change="(ev) => cambiarEstado(r, ev.target.value)"
                    >
                      <option
                        v-for="opt in ESTADOS"
                        :key="opt.value"
                        :value="opt.value"
                      >
                        {{ opt.label }}
                      </option>
                    </select>
                  </td>
                </tr>
              </tbody>
            </table>

            <p
              v-if="!loading && !reclamosFiltrados.length"
              class="rec-empty-text"
            >
              No hay reclamos registrados.
            </p>
          </div>
        </div>
      </template>

      <!-- CLIENTE -->
      <template v-else>
        <div class="rec-tabs">
          <button
            type="button"
            class="rec-tab-btn"
            :class="{ active: modo === 'crear' }"
            @click="modo = 'crear'"
          >
            Crear reclamo
          </button>
          <button
            type="button"
            class="rec-tab-btn"
            :class="{ active: modo === 'mis-reclamos' }"
            @click="modo = 'mis-reclamos'"
          >
            Mis reclamos
          </button>
        </div>

        <!-- Crear reclamo -->
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
                <textarea
                  rows="4"
                  v-model="nuevoReclamo.descripcion"
                ></textarea>
              </div>

              <button type="submit" class="btn-ambos">
                Validar y continuar
              </button>
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
              <button
                type="button"
                class="btn-ambos"
                @click="confirmarCreacion"
              >
                Confirmar y enviar
              </button>
              <button
                type="button"
                class="btn-ambos cancelar"
                @click="cancelarCreacion"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>

        <!-- Mis reclamos -->
        <div v-else class="rec-panel">
          <h3 class="panel-title">Mis reclamos</h3>

          <p v-if="loading">Cargando reclamos...</p>

          <div v-else class="rec-table-wrapper">
            <table class="rec-table">
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
                  <td>{{ r.descripcion }}</td>
                  <td>{{ r.fechaCreacion }}</td>
                  <td>{{ r.estado }}</td>
                </tr>
              </tbody>
            </table>

            <p
              v-if="!loading && !reclamosFiltrados.length"
              class="rec-empty-text"
            >
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
  max-width: 1300px; /* un poco más ancho para que quepa todo cómodo */
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

/* Animación de fade para el modal */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Modal de alertas */
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
  max-width: 420px;
  width: 90%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
  text-align: center;
}

.alert-text {
  margin-bottom: 1rem;
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
  font-weight: 600;
  cursor: pointer;
}

/* Tabs modo cliente */
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
  font-weight: 600;
  color: #555;
  transition: background 0.15s ease, color 0.15s ease;
}
.rec-tab-btn.active {
  background: #ffffff;
  color: var(--cotton-dark, #1c262e);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

/* Panel blanco que contiene la tabla/form */
.rec-panel {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.5rem 1.75rem;
  border: 1px solid #dddddd;
  /* sin overflow: queremos que la tabla se adapte, no scroll */
}

/* Títulos de panel */
.panel-title {
  font-size: 1.1rem;
  margin-bottom: 0.75rem;
  color: var(--cotton-dark, #1c262e);
}

/* Tabla de reclamos: se ajusta al ancho disponible */
.rec-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  table-layout: fixed; /* permite repartir mejor el espacio entre columnas */
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

/* Anchos relativos por columna para que todo entre sin barra */
.rec-table th:nth-child(1),
.rec-table td:nth-child(1) {
  width: 6%;   /* ID */
}
.rec-table th:nth-child(2),
.rec-table td:nth-child(2) {
  width: 18%;  /* Cliente */
}
.rec-table th:nth-child(3),
.rec-table td:nth-child(3) {
  width: 16%;  /* Título */
}
.rec-table th:nth-child(4),
.rec-table td:nth-child(4) {
  width: 38%;  /* Descripción */
}
.rec-table th:nth-child(5),
.rec-table td:nth-child(5) {
  width: 12%;  /* Fecha */
}
.rec-table th:nth-child(6),
.rec-table td:nth-child(6) {
  width: 10%;  /* Estado */
}

.rec-table th {
  color: #1c262e;
  font-weight: 600;
}
.rec-table td {
  color: #333333;
}

/* Descripción: que haga saltos de línea y no rompa el layout */
.rec-table td:nth-child(4) {
  word-break: break-word;
  white-space: normal;
}

/* Select de estado: ancho justo pero sin forzar scroll */
.rec-table td:last-child select {
  width: 100%;
  min-width: 0;
}

/* Bloque de confirmación de reclamo (modo cliente) */
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
