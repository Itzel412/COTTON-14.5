<script setup>
import { ref, computed, onMounted } from 'vue';
import { getProductos, createPedido, getPedidos } from '../data/api';

const props = defineProps({ currentUser: { type: Object, required: true } });

// --- ESTADO ---
const vistaActual = ref('tienda');
const productos = ref([]);
const carrito = ref([]);
const historialAgrupado = ref([]);
const loading = ref(false);

// --- MODAL ---
const modal = ref({ visible: false, tipo: 'info', titulo: '', mensaje: '', accionConfirmar: null });
const mostrarAlerta = (t, m, tipo = 'info') => { modal.value = { visible: true, tipo, titulo: t, mensaje: m, accionConfirmar: null }; };
const mostrarConfirmacion = (t, m, cb) => { modal.value = { visible: true, tipo: 'confirm', titulo: t, mensaje: m, accionConfirmar: cb }; };
const cerrarModal = () => { modal.value.visible = false; };
const ejecutarAccionModal = () => { if (modal.value.accionConfirmar) modal.value.accionConfirmar(); cerrarModal(); };

// --- CARGA ---
const cargarCatalogo = async () => {
  loading.value = true;
  try {
    const data = await getProductos();
    productos.value = Array.isArray(data) ? data : [];
  } catch (e) {
    mostrarAlerta('Error', 'No se pudo cargar el catálogo.', 'error');
  } finally { loading.value = false; }
};

// --- CARRITO ---
const agregarAlCarrito = (p) => {
  if (p.stock <= 0) return;
  const idx = carrito.value.findIndex(i => i.idProducto === p.id && i.color === p.color && i.talla === p.talla);
  
  if (idx !== -1) {
    if (carrito.value[idx].cantidad + 1 > p.stock) return mostrarAlerta('Stock', 'No hay más unidades.');
    carrito.value[idx].cantidad++;
  } else {
    carrito.value.push({
      usuario: props.currentUser.correo,
      idProducto: Number(p.id) || 0,
      color: p.color,
      talla: p.talla,
      cantidad: 1,
      precioUnitario: Number(p.precio),
      displayNombre: `${p.color} - ${p.talla}`
    });
  }
};

const restarDelCarrito = (i) => { if (carrito.value[i].cantidad > 1) carrito.value[i].cantidad--; else carrito.value.splice(i, 1); };
const eliminarDelCarrito = (i) => { carrito.value.splice(i, 1); };
const totalCarrito = computed(() => carrito.value.reduce((acc, i) => acc + (i.precioUnitario * i.cantidad), 0));

// --- COMPRA ---
const solicitarCompra = () => {
  if (!carrito.value.length) return;
  mostrarConfirmacion('Confirmar Pedido', `¿Procesar compra por un total de $${totalCarrito.value.toFixed(2)}?`, procesarCompraBackend);
};

const procesarCompraBackend = async () => {
  loading.value = true;
  try {
    const payload = carrito.value.map(item => ({
      usuario: item.usuario,
      idProducto: Number(item.idProducto),
      color: item.color,
      talla: item.talla,
      cantidad: Number(item.cantidad),
      precioUnitario: Number(item.precioUnitario),
      total: Number(item.precioUnitario) * Number(item.cantidad)
    }));

    const ok = await createPedido(payload);
    
    if (ok) {
      carrito.value = [];
      await cargarCatalogo();
      await cargarHistorial();
      mostrarAlerta('¡Pedido Exitoso!', 'Tu compra ha sido registrada correctamente.');
    } else {
      mostrarAlerta('Rechazado', 'El servidor no pudo procesar el pedido.', 'error');
    }
  } catch (e) {
    console.error(e);
    mostrarAlerta('Error', 'Hubo un error de comunicación.', 'error');
  } finally {
    loading.value = false;
  }
};

// --- HISTORIAL ---
const cargarHistorial = async () => {
  loading.value = true;
  try {
    const todos = await getPedidos();
    const correoUsuario = props.currentUser.correo.toLowerCase().trim();
    const mis = todos.filter(p => p.usuario && p.usuario.toLowerCase().trim() === correoUsuario);
    
    const grupos = {};
    mis.forEach(p => {
      const c = p.codigo || `REF-${p.id}`;
      if (!grupos[c]) grupos[c] = { codigo: c, items: [], total: 0 };
      grupos[c].items.push(p);
      grupos[c].total += p.total;
    });
    historialAgrupado.value = Object.values(grupos).reverse();
  } catch (e) { console.error(e); } finally { loading.value = false; }
};

const solicitarCancelarPedido = (g) => {
  mostrarConfirmacion('Eliminar', `¿Estás seguro de eliminar el pedido ${g.codigo}?`, () => {
    historialAgrupado.value = historialAgrupado.value.filter(x => x.codigo !== g.codigo);
    mostrarAlerta('Eliminado', 'Pedido eliminado del historial.');
  });
};

const cambiarVista = (v) => {
  vistaActual.value = v;
  if (v === 'tienda') cargarCatalogo(); else cargarHistorial();
};
onMounted(cargarCatalogo);
</script>

<template>
  <div class="main-wrapper">
    <div class="tabs-nav">
      <button class="tab-btn" :class="{ active: vistaActual === 'tienda' }" @click="cambiarVista('tienda')">
        🛍️ Catálogo
      </button>
      <button class="tab-btn" :class="{ active: vistaActual === 'historial' }" @click="cambiarVista('historial')">
        📦 Mis Pedidos
      </button>
    </div>

    <div v-if="vistaActual === 'tienda'" class="vista-tienda">
      <div class="bloque-catalogo">
        <h2 class="titulo-seccion">Catálogo de Franelas</h2>
        
        <div v-if="loading && !productos.length" class="loading">
          <div class="spinner"></div> Cargando...
        </div>

        <div v-else class="grid-productos">
          <div v-for="p in productos" :key="p.id" class="card-producto">
            <div class="card-top">
              <span class="badge-talla">{{ p.talla }}</span>
              <span class="badge-stock" :class="{ agotado: p.stock < 5 }">
                {{ p.stock === 0 ? 'AGOTADO' : `Stock: ${p.stock}` }}
              </span>
            </div>
            
            <div class="card-body">
              <h3 class="nombre-producto">{{ p.color }}</h3>
              <p class="desc-producto">Franela de algodón 100%</p>
              <div class="precio-producto">${{ Number(p.precio).toFixed(2) }}</div>
            </div>

            <button class="btn-agregar" :disabled="p.stock === 0" @click="agregarAlCarrito(p)">
              {{ p.stock === 0 ? 'Sin Stock' : 'Agregar al Carrito' }}
            </button>
          </div>
        </div>
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
                  ${{ item.precioUnitario }} x {{ item.cantidad }}
                </div>
              </div>
              <div class="precio-fila">${{ (item.precioUnitario * item.cantidad).toFixed(2) }}</div>
              
              <div class="controles-cantidad">
                 <button @click="restarDelCarrito(i)" class="btn-icon">-</button>
                 <button @click="eliminarDelCarrito(i)" class="btn-icon delete">✕</button>
              </div>
            </div>
          </div>

          <div class="footer-carrito">
            <div class="total-row">
              <span>Total a Pagar</span>
              <span class="monto-total">${{ totalCarrito.toFixed(2) }}</span>
            </div>
            <button class="btn-confirmar" :disabled="carrito.length === 0" @click="solicitarCompra">
              Confirmar Compra
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="vistaActual === 'historial'" class="vista-historial">
       <h2 class="titulo-seccion">Historial de Compras</h2>
       
       <div v-if="loading" class="loading">Cargando...</div>
       <div v-else-if="historialAgrupado.length === 0" class="vacio-msg">
         <p>No tienes pedidos registrados aún.</p>
       </div>

       <div v-else class="grid-historial">
         <div v-for="g in historialAgrupado" :key="g.codigo" class="card-historial">
            <div class="historial-header">
              <div class="order-info">
                <span class="order-id">{{ g.codigo }}</span>
                <span class="order-items">{{ g.items.length }} artículos</span>
              </div>
              <div class="order-total">${{ g.total.toFixed(2) }}</div>
            </div>
            
            <div class="historial-body">
              <ul class="lista-detalles">
                <li v-for="it in g.items" :key="it.id">
                  • <strong>{{ it.color }}</strong> (Talla {{ it.talla }}) 
                  <span v-if="it.cantidad > 1">x{{ it.cantidad }}</span>
                </li>
              </ul>
            </div>
            
            <div class="historial-footer">
              <button @click="solicitarCancelarPedido(g)" class="btn-text-danger">
                Eliminar del historial
              </button>
            </div>
         </div>
       </div>
    </div>

    <div v-if="modal.visible" class="modal-backdrop">
      <div class="modal-card">
        <div class="modal-icon" :class="modal.tipo">
          {{ modal.tipo === 'confirm' ? '?' : (modal.tipo === 'error' ? '!' : '✓') }}
        </div>
        <h3>{{ modal.titulo }}</h3>
        <p>{{ modal.mensaje }}</p>
        <div class="modal-actions">
          <button v-if="modal.tipo === 'confirm'" @click="cerrarModal" class="btn-secondary">Cancelar</button>
          <button @click="ejecutarAccionModal" class="btn-primary">Aceptar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* --- VARIABLES & BASE --- */
.main-wrapper { 
  max-width: 1200px; 
  margin: 0 auto; 
  font-family: 'Segoe UI', sans-serif;
  color: #1c262e;
}

.titulo-seccion {
  font-size: 1.5rem;
  color: #1c262e; /* Dark Blue */
  margin-bottom: 1.5rem;
  font-weight: 700;
}

/* --- TABS --- */
.tabs-nav {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2.5rem;
}

.tab-btn {
  background: transparent;
  border: 2px solid rgba(28, 38, 46, 0.2);
  padding: 0.6rem 1.5rem;
  border-radius: 50px;
  color: #666;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn:hover {
  border-color: #1c262e;
  color: #1c262e;
}

.tab-btn.active {
  background: #1c262e;
  border-color: #1c262e;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(28, 38, 46, 0.2);
}

/* --- LAYOUT TIENDA --- */
.vista-tienda {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.bloque-catalogo {
  flex: 1;
}

.bloque-carrito {
  width: 350px;
  position: sticky;
  top: 1rem;
}

/* --- GRID PRODUCTOS --- */
.grid-productos {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1.5rem;
}

.card-producto {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border: 1px solid #f0f0f0;
}

.card-producto:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.card-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.badge-talla {
  background: #1c262e;
  color: white;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
}

.badge-stock {
  font-size: 0.8rem;
  color: #27ae60;
  font-weight: 600;
}
.badge-stock.agotado { color: #e74c3c; }

.nombre-producto { margin: 0; font-size: 1.2rem; color: #1c262e; }
.desc-producto { margin: 0.2rem 0 1rem 0; font-size: 0.85rem; color: #888; }
.precio-producto { font-size: 1.4rem; font-weight: 800; color: #1c262e; margin-bottom: 1rem; }

.btn-agregar {
  width: 100%;
  padding: 0.8rem;
  background: #1c262e;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-agregar:hover:not(:disabled) { background: #333; }
.btn-agregar:disabled { background: #ddd; cursor: not-allowed; }

/* --- CARRITO --- */
.card-carrito {
  background: #ffffff;
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
}
.carrito-header h3 { margin: 0; color: #1c262e; font-size: 1.1rem; }
.badge-count { background: #e18b6b; color: white; padding: 2px 8px; border-radius: 12px; font-size: 0.85rem; font-weight: bold; }

.lista-carrito { max-height: 400px; overflow-y: auto; }
.item-fila { display: flex; justify-content: space-between; align-items: center; padding: 0.8rem 0; border-bottom: 1px dashed #eee; }
.info-producto strong { display: block; font-size: 0.95rem; color: #333; }
.subtexto { font-size: 0.8rem; color: #888; margin-top: 2px; }
.precio-fila { font-weight: bold; font-size: 0.95rem; color: #1c262e; margin: 0 10px; }

.btn-icon { width: 24px; height: 24px; border-radius: 50%; border: 1px solid #ddd; background: white; color: #555; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; margin-left: 4px; font-size: 0.8rem; }
.btn-icon:hover { background: #f0f0f0; }
.btn-icon.delete { border-color: #ffecec; color: #e74c3c; background: #fff5f5; }
.btn-icon.delete:hover { background: #ffecec; }

.footer-carrito { margin-top: 1.5rem; padding-top: 1rem; border-top: 2px solid #f0f0f0; }
.total-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; font-size: 1.1rem; }
.monto-total { font-weight: 800; font-size: 1.4rem; color: #1c262e; }

.btn-confirmar {
  width: 100%;
  padding: 1rem;
  background: #1c262e; /* Cotton Dark */
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.1s;
}
.btn-confirmar:hover:not(:disabled) { transform: translateY(-2px); background: #333; }
.btn-confirmar:disabled { background: #ccc; cursor: not-allowed; }

/* --- HISTORIAL --- */
.grid-historial { display: flex; flex-direction: column; gap: 1.5rem; }
.card-historial {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
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
.order-id { font-family: monospace; font-weight: bold; background: #e0e0e0; padding: 3px 6px; border-radius: 4px; color: #333; }
.order-items { font-size: 0.85rem; color: #666; margin-left: 10px; }
.order-total { font-weight: 800; font-size: 1.2rem; color: #1c262e; }

.historial-body { padding: 1.5rem; }
.lista-detalles { list-style: none; padding: 0; margin: 0; color: #555; font-size: 0.95rem; }
.lista-detalles li { margin-bottom: 0.4rem; }

.historial-footer { padding: 0.8rem 1.5rem; background: white; text-align: right; border-top: 1px solid #f9f9f9; }
.btn-text-danger { background: none; border: none; color: #e74c3c; font-weight: 600; cursor: pointer; font-size: 0.9rem; }
.btn-text-danger:hover { text-decoration: underline; }

/* --- MODAL --- */
.modal-backdrop {
  position: fixed; inset: 0; background: rgba(28, 38, 46, 0.6);
  display: flex; justify-content: center; align-items: center; z-index: 1000;
  backdrop-filter: blur(3px);
}
.modal-card {
  background: white; padding: 2rem; border-radius: 20px; width: 90%; max-width: 400px;
  text-align: center; box-shadow: 0 15px 40px rgba(0,0,0,0.2); animation: popIn 0.3s ease;
}
@keyframes popIn { from { transform: scale(0.9); opacity: 0; } to { transform: scale(1); opacity: 1; } }

.modal-icon { width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem; font-size: 1.5rem; font-weight: bold; }
.modal-icon.info { background: #e3f2fd; color: #2196f3; }
.modal-icon.error { background: #ffebee; color: #e53935; }
.modal-icon.confirm { background: #fff3e0; color: #ff9800; }

.modal-card h3 { margin: 0 0 0.5rem; color: #1c262e; }
.modal-card p { color: #666; margin-bottom: 1.5rem; line-height: 1.5; }

.modal-actions { display: flex; gap: 1rem; justify-content: center; }
.btn-primary { background: #1c262e; color: white; padding: 0.8rem 1.5rem; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; }
.btn-secondary { background: #f0f0f0; color: #333; padding: 0.8rem 1.5rem; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; }

/* Responsivo */
@media (max-width: 850px) {
  .vista-tienda { flex-direction: column; }
  .bloque-carrito { width: 100%; position: static; margin-top: 2rem; }
}
</style>