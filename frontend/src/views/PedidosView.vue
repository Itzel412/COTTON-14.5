<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import {
  getProductos,
  getPedidos,
  createPedido,
  updatePedido,
  deletePedido,
  deletePedidoPorCodigo
} from '../data/api';

const props = defineProps({
  currentUser: { type: Object, required: true },
});

const esAdmin = computed(() => props.currentUser?.rol === 'ADMIN');

const productos = ref([]);
const pedidos = ref([]);

const vistaCliente = ref('tienda'); 
const carrito = ref([]);
const historialAgrupado = ref([]);

const loadingProductos = ref(false);
const loadingPedidos = ref(false);
const loadingAccion = ref(false);

const error = ref(null);
const mensaje = ref(null);

const modal = ref({
  visible: false,
  tipo: 'info', 
  titulo: '',
  mensaje: '',
  accionConfirmar: null,
});

const editForm = ref({
  id: null,
  codigo: '',
  color: '',
  talla: '',
  cantidad: 1,
  usuario: '',
  idProducto: 0,
  fecha: '',
  
  idProductoOriginal: 0,
  cantidadOriginal: 0,
  maxCantidad: 1,
});
const editOptions = ref({
  colores: [],
  tallas: [],
});
const abrirModalInfo = (titulo, msg) => {
  modal.value = { visible: true, tipo: 'info', titulo, mensaje: msg, accionConfirmar: null };
};
const abrirModalError = (titulo, msg) => {
  modal.value = { visible: true, tipo: 'error', titulo, mensaje: msg, accionConfirmar: null };
};
const abrirModalConfirm = (titulo, msg, cb) => {
  modal.value = { visible: true, tipo: 'confirm', titulo, mensaje: msg, accionConfirmar: cb };
};

const cerrarModal = () => {
  modal.value.visible = false;
};
const norm = (s) => (s ?? '').toString().trim();
const normLower = (s) => norm(s).toLowerCase();
const normTalla = (s) => norm(s).toUpperCase();

const uniq = (arr) => Array.from(new Set(arr));

const disponibilidadParaProducto = (prodId) => {
  const p = productos.value.find((x) => Number(x.id) === Number(prodId));
  if (!p) return 0;

  const baseStock = Number(p.stock ?? 0);
  const bonus = Number(editForm.value.idProductoOriginal) === Number(p.id)
    ? Number(editForm.value.cantidadOriginal ?? 0)
    : 0;

  return baseStock + bonus;
};

const recalcularOpcionesEdit = () => {
  if (!productos.value.length) {
    editOptions.value = { colores: [], tallas: [] };
    editForm.value.maxCantidad = 1;
    return;
  }

  const variantesDisponibles = productos.value
    .map((p) => ({
      ...p,
      disponible: disponibilidadParaProducto(p.id),
      colorN: normLower(p.color),
      tallaN: normTalla(p.talla),
    }))
    .filter((p) => Number(p.disponible) > 0);

  const colores = uniq(variantesDisponibles.map((v) => v.color)).sort((a, b) => a.localeCompare(b));
  editOptions.value.colores = colores;

  const colorSel = norm(editForm.value.color);
  const colorOk = colores.some((c) => normLower(c) === normLower(colorSel));
  if (!colorOk) {
    editForm.value.color = colores[0] || '';
  }

  const variantesPorColor = variantesDisponibles.filter(
    (v) => normLower(v.color) === normLower(editForm.value.color)
  );
  const tallas = uniq(variantesPorColor.map((v) => v.tallaN)).sort();
  editOptions.value.tallas = tallas;

  const tallaSel = normTalla(editForm.value.talla);
  const tallaOk = tallas.includes(tallaSel);
  if (!tallaOk) {
    editForm.value.talla = tallas[0] || '';
  } else {
    editForm.value.talla = tallaSel;
  }

  const match = variantesDisponibles.find(
    (v) => normLower(v.color) === normLower(editForm.value.color) && v.tallaN === normTalla(editForm.value.talla)
  );

  if (!match) {
    editForm.value.maxCantidad = 1;
    editForm.value.cantidad = 1;
    editForm.value.idProducto = Number(editForm.value.idProductoOriginal || 0);
    return;
  }

  editForm.value.idProducto = Number(match.id);
  editForm.value.maxCantidad = Number(match.disponible || 1);

  if (Number(editForm.value.cantidad) > Number(editForm.value.maxCantidad)) {
    editForm.value.cantidad = Number(editForm.value.maxCantidad);
  }
  if (Number(editForm.value.cantidad) < 1) {
    editForm.value.cantidad = 1;
  }
};

const abrirModalEdit = async (pedidoItem) => {
  if (!productos.value.length) {
    loadingProductos.value = true;
    try {
      productos.value = await getProductos();
    } catch (e) {
      abrirModalError('Error', e.message || 'No se pudo cargar el inventario para editar.');
      loadingProductos.value = false;
      return;
    } finally {
      loadingProductos.value = false;
    }
  }

  editForm.value = {
    id: pedidoItem.id,
    codigo: pedidoItem.codigo,
    color: pedidoItem.color,
    talla: pedidoItem.talla,
    cantidad: Number(pedidoItem.cantidad || 1),
    usuario: pedidoItem.usuario,
    idProducto: Number(pedidoItem.idProducto || 0),
    fecha: pedidoItem.fecha,

    idProductoOriginal: Number(pedidoItem.idProducto || 0),
    cantidadOriginal: Number(pedidoItem.cantidad || 0),

    maxCantidad: 1,
  };

  modal.value = { visible: true, tipo: 'edit', titulo: 'Editar item', mensaje: '', accionConfirmar: null };

  recalcularOpcionesEdit();
};

watch(
  () => [modal.value.visible, modal.value.tipo, editForm.value.color, editForm.value.talla],
  () => {
    if (modal.value.visible && modal.value.tipo === 'edit') {
      recalcularOpcionesEdit();
    }
  }
);

const cargarProductos = async () => {
  if (esAdmin.value) return;
  loadingProductos.value = true;
  try {
    productos.value = await getProductos();
  } catch (e) {
    abrirModalError('Error', e.message || 'No se pudo cargar el catálogo.');
  } finally {
    loadingProductos.value = false;
  }
};

const cargarPedidos = async () => {
  loadingPedidos.value = true;
  try {
    pedidos.value = await getPedidos();
    if (!esAdmin.value) construirHistorialAgrupado();
  } catch (e) {
    abrirModalError('Error', e.message || 'No se pudo cargar pedidos.');
  } finally {
    loadingPedidos.value = false;
  }
};

const pedidosFiltrados = computed(() => {
  if (esAdmin.value) return pedidos.value;
  const correo = (props.currentUser?.correo || '').toLowerCase().trim();
  return pedidos.value.filter((p) => (p.usuario || '').toLowerCase().trim() === correo);
});

const construirHistorialAgrupado = () => {
  const mis = pedidosFiltrados.value;
  const grupos = {};
  mis.forEach((p) => {
    const c = p.codigo || `REF-${p.id}`;
    if (!grupos[c]) grupos[c] = { codigo: c, items: [], total: 0, fecha: p.fecha || '' };
    grupos[c].items.push(p);
    grupos[c].total += Number(p.total || 0);
    if (!grupos[c].fecha && p.fecha) grupos[c].fecha = p.fecha;
  });
  historialAgrupado.value = Object.values(grupos).reverse();
};

const totalCarrito = computed(() =>
  carrito.value.reduce((acc, it) => acc + Number(it.precioUnitario || 0) * Number(it.cantidad || 0), 0)
);

const agregarAlCarrito = (p) => {
  if (!p || p.stock <= 0) return;

  const idx = carrito.value.findIndex(
    (i) => i.idProducto === p.id && i.color === p.color && i.talla === p.talla
  );

  if (idx !== -1) {
    if (carrito.value[idx].cantidad + 1 > p.stock) {
      abrirModalInfo('Stock', 'No hay más unidades disponibles.');
      return;
    }
    carrito.value[idx].cantidad++;
  } else {
    carrito.value.push({
      usuario: props.currentUser.correo,
      idProducto: Number(p.id) || 0,
      color: p.color,
      talla: p.talla,
      cantidad: 1,
      precioUnitario: Number(p.precio),
      total: Number(p.precio),
      fecha: new Date().toISOString().split('T')[0],
      displayNombre: `${p.color} - ${p.talla}`,
    });
  }

  carrito.value = carrito.value.map((it) => ({
    ...it,
    total: Number(it.precioUnitario) * Number(it.cantidad),
  }));
};

const restarDelCarrito = (i) => {
  if (carrito.value[i].cantidad > 1) carrito.value[i].cantidad--;
  else carrito.value.splice(i, 1);

  carrito.value = carrito.value.map((it) => ({
    ...it,
    total: Number(it.precioUnitario) * Number(it.cantidad),
  }));
};

const eliminarDelCarrito = (i) => {
  carrito.value.splice(i, 1);
};

const solicitarCompra = () => {
  if (!carrito.value.length) return;
  abrirModalConfirm(
    'Confirmar Pedido',
    `¿Procesar compra por un total de $${totalCarrito.value.toFixed(2)}?`,
    procesarCompraBackend
  );
};

const procesarCompraBackend = async () => {
  loadingAccion.value = true;
  try {
    const payload = carrito.value.map((item) => ({
      usuario: item.usuario,
      idProducto: Number(item.idProducto),
      color: item.color,
      talla: item.talla,
      cantidad: Number(item.cantidad),
      precioUnitario: Number(item.precioUnitario),
      total: Number(item.precioUnitario) * Number(item.cantidad),
      fecha: item.fecha,
    }));

    const ok = await createPedido(payload);
    if (!ok) {
      abrirModalError('Rechazado', 'El servidor no pudo procesar el pedido.');
      return;
    }

    carrito.value = [];
    await Promise.all([cargarProductos(), cargarPedidos()]);
    abrirModalInfo('¡Pedido Exitoso!', 'Tu compra ha sido registrada correctamente.');
  } catch (e) {
    abrirModalError('Error', e.message || 'Hubo un error de comunicación.');
  } finally {
    loadingAccion.value = false;
  }
};

const solicitarEliminarPedidoCompleto = (grupo) => {
  abrirModalConfirm(
    'Eliminar pedido',
    `¿Seguro que deseas eliminar el pedido ${grupo.codigo}? Esta acción restaurará el stock.`,
    async () => {
      loadingAccion.value = true;
      try {
        const ok = await deletePedidoPorCodigo(grupo.codigo);
        if (!ok) {
          abrirModalError('Error', 'No se pudo eliminar el pedido completo.');
          return;
        }
        await Promise.all([cargarProductos(), cargarPedidos()]);
        abrirModalInfo('Eliminado', `Pedido ${grupo.codigo} eliminado correctamente.`);
      } catch (e) {
        abrirModalError('Error', e.message || 'No se pudo eliminar el pedido.');
      } finally {
        loadingAccion.value = false;
      }
    }
  );
};

const solicitarEliminarItem = (item) => {
  abrirModalConfirm(
    'Eliminar item',
    `¿Seguro que deseas eliminar este item del pedido ${item.codigo}?`,
    async () => {
      loadingAccion.value = true;
      try {
        const ok = await deletePedido(item.id);
        if (!ok) {
          abrirModalError('Error', 'No se pudo eliminar el item.');
          return;
        }
        await Promise.all([cargarProductos(), cargarPedidos()]);
        abrirModalInfo('Eliminado', 'Item eliminado correctamente.');
      } catch (e) {
        abrirModalError('Error', e.message || 'No se pudo eliminar el item.');
      } finally {
        loadingAccion.value = false;
      }
    }
  );
};

const guardarEdicionItem = async () => {
  if (!editForm.value.id || editForm.value.id <= 0) {
    abrirModalError('Error', 'ID inválido.');
    return;
  }

  if (!editOptions.value.colores.length || !editOptions.value.tallas.length) {
    abrirModalError('Sin stock', 'No hay variantes disponibles para actualizar este item.');
    return;
  }

  const cantidad = Number(editForm.value.cantidad || 1);
  if (cantidad < 1 || cantidad > Number(editForm.value.maxCantidad || 1)) {
    abrirModalError('Cantidad inválida', `La cantidad debe estar entre 1 y ${editForm.value.maxCantidad}.`);
    return;
  }

  loadingAccion.value = true;
  try {
    const payload = {
      id: Number(editForm.value.id),
      codigo: editForm.value.codigo,
      usuario: editForm.value.usuario,
      idProducto: Number(editForm.value.idProducto),
      color: editForm.value.color,
      talla: editForm.value.talla,
      cantidad: cantidad,
      fecha: editForm.value.fecha,
    };

    const ok = await updatePedido(payload);
    if (!ok) {
      abrirModalError('Error', 'El backend rechazó la actualización (validación/stock).');
      return;
    }

    cerrarModal();
    await Promise.all([cargarProductos(), cargarPedidos()]);
    abrirModalInfo('Actualizado', 'Item actualizado correctamente.');
  } catch (e) {
    abrirModalError('Error', e.message || 'No se pudo actualizar el item.');
  } finally {
    loadingAccion.value = false;
  }
};

const cambiarVistaCliente = async (v) => {
  vistaCliente.value = v;

  if (!esAdmin.value) {
    await cargarProductos();
  }
  await cargarPedidos();
};

onMounted(async () => {
  if (!esAdmin.value) await cargarProductos();
  await cargarPedidos();
});
</script>

<template>
  <section class="pedidos-wrapper">
    <div class="pedidos-card">
      <header class="pedidos-header">
        <h2 v-if="esAdmin">Gestión de pedidos</h2>
        <h2 v-else>Pedidos</h2>

        <p v-if="esAdmin">Consulta pedidos.</p>
        <p v-else>Compra y gestiona tus pedidos.</p>
      </header>

      <div v-if="modal.visible" class="modal-backdrop">
        <div class="modal-card">
          <h3>{{ modal.titulo }}</h3>

          <template v-if="modal.tipo !== 'edit'">
            <p>{{ modal.mensaje }}</p>
          </template>

          <template v-else>
            <p class="hint">
              Puedes cambiar <strong>color</strong>, <strong>talla</strong> y <strong>cantidad</strong> según disponibilidad.
            </p>

            <div v-if="loadingProductos" class="hint">Cargando inventario...</div>

            <div v-else-if="!editOptions.colores.length" class="hint error">
              No hay stock disponible para modificar este item (solo puedes eliminarlo).
            </div>

            <div v-else class="form-grid">
              <div class="form-group">
                <label>Color</label>
                <select v-model="editForm.color">
                  <option v-for="c in editOptions.colores" :key="c" :value="c">{{ c }}</option>
                </select>
              </div>

              <div class="form-group">
                <label>Talla</label>
                <select v-model="editForm.talla">
                  <option v-for="t in editOptions.tallas" :key="t" :value="t">{{ t }}</option>
                </select>
              </div>

              <div class="form-group">
                <label>Cantidad <span class="max"> (máx {{ editForm.maxCantidad }})</span></label>
                <input
                  v-model.number="editForm.cantidad"
                  type="number"
                  min="1"
                  :max="editForm.maxCantidad"
                />
              </div>
            </div>
          </template>

          <div class="modal-actions">
            <button v-if="modal.tipo === 'confirm'" class="btn-secondary" @click="cerrarModal">
              Cancelar
            </button>

            <button
              v-if="modal.tipo === 'confirm'"
              class="btn-primary"
              @click="() => { if (modal.accionConfirmar) modal.accionConfirmar(); cerrarModal(); }"
            >
              Aceptar
            </button>

            <button
              v-else-if="modal.tipo === 'edit'"
              class="btn-primary"
              :disabled="loadingAccion || (!editOptions.colores.length)"
              @click="guardarEdicionItem"
            >
              {{ loadingAccion ? 'Guardando...' : 'Guardar' }}
            </button>

            <button v-else class="btn-primary" @click="cerrarModal">
              OK
            </button>
          </div>
        </div>
      </div>

      <template v-if="esAdmin">
        <div class="ped-panel">
          <h3 class="panel-title">Pedidos registrados</h3>
          <p v-if="loadingPedidos">Cargando pedidos...</p>

          <table v-else class="ped-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Código</th>
                <th>Cliente</th>
                <th>Color</th>
                <th>Talla</th>
                <th>Cantidad</th>
                <th>Precio unitario</th>
                <th>Total</th>
                <th>Fecha</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in pedidosFiltrados" :key="p.id">
                <td>{{ p.id }}</td>
                <td>{{ p.codigo }}</td>
                <td>{{ p.usuario }}</td>
                <td>{{ p.color }}</td>
                <td>{{ p.talla }}</td>
                <td>{{ p.cantidad }}</td>
                <td>{{ Number(p.precioUnitario).toFixed(2) }} $</td>
                <td>{{ Number(p.total).toFixed(2) }} $</td>
                <td>{{ p.fecha }}</td>
              </tr>
            </tbody>
          </table>

          <p v-if="!loadingPedidos && !pedidosFiltrados.length">No hay pedidos registrados.</p>
        </div>
      </template>

      <template v-else>
        <div class="tabs-nav">
          <button class="tab-btn" :class="{ active: vistaCliente === 'tienda' }" @click="cambiarVistaCliente('tienda')">
            Catálogo
          </button>
          <button class="tab-btn" :class="{ active: vistaCliente === 'historial' }" @click="cambiarVistaCliente('historial')">
            Mis pedidos
          </button>
        </div>

        <div v-if="vistaCliente === 'tienda'" class="vista-tienda">
          <div class="bloque-catalogo">
            <h3 class="panel-title">Catálogo de franelas</h3>

            <p v-if="loadingProductos">Cargando productos...</p>

            <div v-else class="catalog-grid">
              <article v-for="p in productos" :key="p.id" class="prod-card">
                <h4>{{ p.color }} · Talla {{ p.talla }}</h4>
                <p class="prod-price">{{ Number(p.precio).toFixed(2) }} $</p>
                <p class="prod-stock">Stock disponible: {{ p.stock }}</p>
                <button class="btn-ambos prod-btn" :disabled="p.stock === 0" @click="agregarAlCarrito(p)">
                  {{ p.stock === 0 ? 'Sin stock' : 'Agregar al carrito' }}
                </button>
              </article>
            </div>

            <p v-if="!loadingProductos && !productos.length">No hay productos cargados en el catálogo.</p>
          </div>

          <div class="bloque-carrito">
            <div class="card-carrito">
              <div class="carrito-header">
                <h3>Tu Carrito</h3>
                <span class="badge-count">{{ carrito.length }}</span>
              </div>

              <div v-if="carrito.length === 0" class="carrito-vacio">
                <p>Tu carrito está vacío.</p>
                <small>Agrega productos para comenzar.</small>
              </div>

              <div v-else class="lista-carrito">
                <div v-for="(item, i) in carrito" :key="i" class="item-fila">
                  <div class="info-producto">
                    <strong>{{ item.displayNombre }}</strong>
                    <div class="subtexto">
                      ${{ Number(item.precioUnitario).toFixed(2) }} x {{ item.cantidad }}
                    </div>
                  </div>

                  <div class="precio-fila">
                    ${{ (Number(item.precioUnitario) * Number(item.cantidad)).toFixed(2) }}
                  </div>

                  <div class="controles-cantidad">
                    <button class="btn-icon" @click="restarDelCarrito(i)">-</button>
                    <button class="btn-icon delete" @click="eliminarDelCarrito(i)">✕</button>
                  </div>
                </div>
              </div>

              <div class="footer-carrito">
                <div class="total-row">
                  <span>Total a Pagar</span>
                  <span class="monto-total">${{ totalCarrito.toFixed(2) }}</span>
                </div>
                <button class="btn-confirmar" :disabled="carrito.length === 0 || loadingAccion" @click="solicitarCompra">
                  {{ loadingAccion ? 'Procesando...' : 'Confirmar compra' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="ped-panel">
          <h3 class="panel-title">Historial de compras</h3>

          <p v-if="loadingPedidos">Cargando pedidos...</p>

          <div v-else-if="historialAgrupado.length === 0" class="vacio-msg">
            <p>No tienes pedidos registrados aún.</p>
          </div>

          <div v-else class="grid-historial">
            <div v-for="g in historialAgrupado" :key="g.codigo" class="card-historial">
              <div class="historial-header">
                <div class="order-info">
                  <span class="order-id">{{ g.codigo }}</span>
                  <span class="order-items">{{ g.items.length }} artículos</span>
                  <span v-if="g.fecha" class="order-items">· {{ g.fecha }}</span>
                </div>
                <div class="order-total">${{ g.total.toFixed(2) }}</div>
              </div>

              <div class="historial-body">
                <table class="ped-table">
                  <thead>
                    <tr>
                      <th>Item ID</th>
                      <th>Color</th>
                      <th>Talla</th>
                      <th>Cant</th>
                      <th>Total</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="it in g.items" :key="it.id">
                      <td>{{ it.id }}</td>
                      <td>{{ it.color }}</td>
                      <td>{{ it.talla }}</td>
                      <td>{{ it.cantidad }}</td>
                      <td>${{ Number(it.total).toFixed(2) }}</td>
                      <td class="acciones">
                        <button class="icon-btn edit" title="Editar" @click="abrirModalEdit(it)">
                          <img src="/icon-edit.png" alt="Editar" />
                        </button>

                        <button class="icon-btn delete" title="Eliminar" @click="solicitarEliminarItem(it)">
                          <img src="/icon-delete.png" alt="Eliminar" />
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="historial-footer">
                <button class="btn-text-danger" @click="solicitarEliminarPedidoCompleto(g)">
                  Eliminar pedido completo
                </button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.pedidos-wrapper {
  padding: 2.5rem 1rem 3rem;
  display: flex;
  justify-content: center;
}
.pedidos-card {
  background: var(--cotton-light, #fcf5e9);
  border-radius: 20px;
  padding: 2rem 1.75rem;
  max-width: 1200px;
  width: 100%;
  box-shadow: 0 10px 30px rgba(0,0,0,0.18);
}
.pedidos-header {
  margin-bottom: 1.5rem;
}
.pedidos-header h2 {
  font-size: 1.6rem;
  margin-bottom: 0.3rem;
  color: #111; 
}
.pedidos-header p {
  color: #222; 
  font-size: 0.95rem;
}

.ped-panel {
  background: #fff;
  border-radius: 16px;
  padding: 1.5rem;
  border: 1px solid #ddd;
}
.panel-title {
  font-size: 1.1rem;
  margin-bottom: 0.75rem;
  color: #111; 
}
.ped-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  color: #111; 
}
.ped-table thead {
  background: #f0f0f0;
}
.ped-table th, .ped-table td {
  padding: 0.55rem 0.6rem;
  border-bottom: 1px solid #e4e4e4;
  text-align: left;
  color: #111; 
}
.acciones {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.icon-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 10px 18px rgba(0, 0, 0, 0.18);
  transition: transform 0.12s ease, box-shadow 0.12s ease, filter 0.12s ease;
}

.icon-btn img {
  width: 18px;
  height: 18px;
  display: block;
}

.icon-btn.edit {
  background: #e9cfad; 
}

.icon-btn.delete {
  background: #c8421a; 
}

.icon-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 22px rgba(0, 0, 0, 0.22);
  filter: brightness(1.02);
}

.icon-btn:active {
  transform: translateY(0);
  box-shadow: 0 8px 14px rgba(0, 0, 0, 0.18);
}

.icon-btn:focus {
  outline: 3px solid rgba(28, 38, 46, 0.25);
  outline-offset: 2px;
}

.tabs-nav {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
}
.tab-btn {
  background: transparent;
  border: 2px solid rgba(28, 38, 46, 0.2);
  padding: 0.6rem 1.5rem;
  border-radius: 50px;
  color: #111; 
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}
.tab-btn.active {
  background: #1c262e;
  border-color: #1c262e;
  color: #fff;
}
.vista-tienda {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}
.bloque-catalogo { flex: 1; }
.bloque-carrito {
  width: 360px;
  position: sticky;
  top: 1rem;
}
.catalog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 1rem;
}
.prod-card {
  background: #fff;
  border-radius: 14px;
  padding: 1rem;
  border: 1px solid #e2e2e2;
  box-shadow: 0 4px 10px rgba(0,0,0,0.04);
  color: #111;
}
.prod-card h4 { 
  margin: 0 0 0.25rem; 
  color: #111; 
}
.prod-price { 
  font-weight: 800;
  margin-bottom: 0.25rem; 
  color: #111; }
.prod-stock { 
  font-size: 0.85rem; 
  color: #222; 
  margin-bottom: 0.6rem; 
}
.btn-ambos {
  background: #1c262e;
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 0.6rem 1rem;
  font-weight: 800;
  cursor: pointer;
}
.btn-ambos:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.prod-btn { width: 100%; }

.card-carrito {
  color: #111;
  background: #fff;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}
.carrito-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
  margin-bottom: 1rem;
  color: #111;
}
.badge-count {
  background: #e18b6b;
  color: #fff;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 800;
}
.lista-carrito {
  max-height: 420px;
  overflow-y: auto;
}
.item-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 0;
  border-bottom: 1px dashed #eee;
}
.subtexto { 
  font-size: 0.8rem; 
  color: #222;
  margin-top: 2px; 
}
.precio-fila { font-weight: 900; color: #111; margin: 0 10px; }
.btn-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  margin-left: 4px;
}
.btn-icon.delete {
  border-color: #ffecec;
  color: #e74c3c;
  background: #fff5f5;
}
.footer-carrito {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 2px solid #f0f0f0;
  color: #111;
}
.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}
.monto-total {
  font-weight: 900;
  font-size: 1.4rem;
  color: #111;
}
.btn-confirmar {
  width: 100%;
  padding: 1rem;
  background: #1c262e;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 900;
  cursor: pointer;
}
.btn-confirmar:disabled { 
  background: #ccc; 
  cursor: not-allowed; 
}

.grid-historial {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.card-historial {
  color: #111;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eee;
  overflow: hidden;
}
.historial-header {
  background: #fcfbf9;
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}
.order-id {
  font-family: monospace;
  font-weight: 900;
  background: #e0e0e0;
  padding: 3px 6px;
  border-radius: 4px;
  color: #111;
}
.order-items {
  font-size: 0.85rem; 
  color: #222; 
  margin-left: 10px; 
}
.order-total { 
  font-weight: 900; 
  font-size: 1.2rem;
  color: #111; 
  }
.historial-body { 
  padding: 1rem 1.5rem; 
}
.historial-footer {
  padding: 0.8rem 1.5rem;
  text-align: right;
  border-top: 1px solid #f3f3f3;
}
.btn-text-danger {
  background: none;
  border: none;
  color: #e74c3c;
  font-weight: 900;
  cursor: pointer;
}
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(28,38,46,0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-card {
  background: #fff;
  padding: 1.6rem;
  border-radius: 16px;
  width: min(520px, 92%);
  box-shadow: 0 15px 40px rgba(0,0,0,0.2);
  color: #111;
}
.hint {
  margin: 0.5rem 0 1rem;
  color: #222;
  font-size: 0.95rem;
}
.hint.error { color: #b10000; font-weight: 800; }
.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1rem;
}
.btn-primary {
  background: #1c262e;
  color: #fff;
  padding: 0.7rem 1.2rem;
  border: none;
  border-radius: 10px;
  font-weight: 900;
  cursor: pointer;
}
.btn-primary:disabled { background: #999; cursor: not-allowed; }
.btn-secondary {
  background: #f0f0f0;
  color: #111;
  padding: 0.7rem 1.2rem;
  border: none;
  border-radius: 10px;
  font-weight: 900;
  cursor: pointer;
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.9rem;
  margin-top: 0.6rem;
}
.form-group label {
  display: block;
  font-weight: 900;
  margin-bottom: 0.25rem;
  color: #111;
}
.form-group input,
.form-group select {
  width: 100%;
  padding: 0.65rem;
  border-radius: 12px;
  border: 1px solid #ddd;
  background: #fff;
  color: #111;
  font-weight: 700;
}
.max { font-weight: 800; color: #444; }

@media (max-width: 900px) {
  .vista-tienda { flex-direction: column; }
  .bloque-carrito { width: 100%; position: static; }
  .form-grid { grid-template-columns: 1fr; }
}
</style>
