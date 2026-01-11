const API_BASE_URL = 'http://localhost:8080/api/perfil';
const INVENTARIO_BASE_URL = 'http://localhost:8080/api/inventario';
const PEDIDOS_BASE_URL = 'http://localhost:8080/api/pedidos';
const FACTURAS_BASE_URL = 'http://localhost:8080/api/facturas';
const RECLAMOS_BASE_URL = 'http://localhost:8080/api/reclamos';

const headersJson = () => ({
  'Content-Type': 'application/json',
  Accept: 'application/json',
});

const headersUsuario = (currentUser) => ({
  ...headersJson(),
  'X-User-Email': currentUser?.correo || '',
  'X-User-Role': currentUser?.rol || '',
});

// Para requests sin body (GET/DELETE) evitamos Content-Type
const headersUsuarioSinBody = (currentUser) => ({
  Accept: 'application/json',
  'X-User-Email': currentUser?.correo || '',
  'X-User-Role': currentUser?.rol || '',
});

async function throwIfNotOk(response, defaultMsg) {
  if (response.ok) return;
  const texto = await response.text().catch(() => '');
  throw new Error(texto || defaultMsg);
}

// 1. GESTIÓN DE USUARIOS
export async function loginRequest(email, password) {
  const response = await fetch(`${API_BASE_URL}/login`, {
    method: 'POST',
    headers: headersJson(),
    body: JSON.stringify({ correo: email, clave: password }),
  });

  await throwIfNotOk(response, 'Error al iniciar sesión');
  return await response.json();
}

export async function getPerfiles() {
  const response = await fetch(`${API_BASE_URL}/todos`);
  await throwIfNotOk(response, 'Error al obtener perfiles');
  return await response.json();
}

export async function createPerfil(nuevoPerfil) {
  const response = await fetch(`${API_BASE_URL}/registrar`, {
    method: 'POST',
    headers: headersJson(),
    body: JSON.stringify(nuevoPerfil),
  });

  await throwIfNotOk(response, 'Error al registrar perfil');
  await response.json().catch(() => null);
  return await getPerfiles();
}

export async function registerClientePerfil(datos) {
  const payload = {
    nombre: datos.nombre,
    correo: datos.correo,
    clave: datos.clave,
    direccion: datos.direccion,
    telefono: datos.telefono,
    rol: 'CLIENTE',
  };

  const response = await fetch(`${API_BASE_URL}/registrar`, {
    method: 'POST',
    headers: headersJson(),
    body: JSON.stringify(payload),
  });

  await throwIfNotOk(response, 'Error al registrar cliente');
  await response.json().catch(() => null);
  return await loginRequest(payload.correo, payload.clave);
}

// 3. INVENTARIO
export async function getProductos() {
  const response = await fetch(`${INVENTARIO_BASE_URL}/productos`);
  await throwIfNotOk(response, 'Error al obtener el catálogo');
  return await response.json();
}

export async function createProducto(producto) {
  const response = await fetch(`${INVENTARIO_BASE_URL}/productos`, {
    method: 'POST',
    headers: headersJson(),
    body: JSON.stringify(producto),
  });

  await throwIfNotOk(response, 'Error al registrar el producto');
  return await response.json();
}

export async function updateProducto(producto) {
  const id = producto?.id;
  if (!id) throw new Error('El producto no tiene id para editar');

  const response = await fetch(`${INVENTARIO_BASE_URL}/productos/${id}`, {
    method: 'PUT',
    headers: headersJson(),
    body: JSON.stringify(producto),
  });

  await throwIfNotOk(response, 'Error al editar el producto');
  return await response.json();
}

export async function deleteProducto(id) {
  if (!id) throw new Error('El id es obligatorio para eliminar');

  const response = await fetch(`${INVENTARIO_BASE_URL}/productos/${id}`, { method: 'DELETE' });
  await throwIfNotOk(response, 'Error al eliminar el producto');
  return await response.json();
}

// 4. PEDIDOS
export async function createPedido(listaDePedidos) {
  const response = await fetch(PEDIDOS_BASE_URL, {
    method: 'POST',
    headers: headersJson(),
    body: JSON.stringify(listaDePedidos),
  });

  await throwIfNotOk(response, 'Error al procesar el pedido');
  return await response.json();
}

export async function getPedidos() {
  const response = await fetch(PEDIDOS_BASE_URL);
  await throwIfNotOk(response, 'Error al obtener pedidos');
  return await response.json();
}

export async function updatePedido(pedido) {
  const response = await fetch(PEDIDOS_BASE_URL, {
    method: 'PUT',
    headers: headersJson(),
    body: JSON.stringify(pedido),
  });

  await throwIfNotOk(response, 'Error al actualizar pedido');
  return await response.json();
}

export async function deletePedido(id) {
  const response = await fetch(`${PEDIDOS_BASE_URL}/${id}`, { method: 'DELETE' });
  await throwIfNotOk(response, 'Error al eliminar item del pedido');
  return await response.json();
}

export async function deletePedidoPorCodigo(codigo) {
  const response = await fetch(`${PEDIDOS_BASE_URL}/codigo/${encodeURIComponent(codigo)}`, {
    method: 'DELETE',
  });

  await throwIfNotOk(response, 'Error al eliminar pedido completo');
  return await response.json();
}

// 5. FACTURAS
export async function getFacturas() {
  const response = await fetch(FACTURAS_BASE_URL);
  await throwIfNotOk(response, 'Error al obtener facturas');
  return await response.json();
}

export async function createFactura(datos) {
  const response = await fetch(FACTURAS_BASE_URL, {
    method: 'POST',
    headers: headersJson(),
    body: JSON.stringify(datos),
  });

  await throwIfNotOk(response, 'Error al generar la factura');
  return await response.json();
}

export async function deleteFactura(id) {
  const response = await fetch(`${FACTURAS_BASE_URL}/${id}`, { method: 'DELETE' });
  await throwIfNotOk(response, 'Error al eliminar la factura');
  return await response.json();
}

export async function updateFacturaEstado(id, nuevoEstado) {
  const response = await fetch(`${FACTURAS_BASE_URL}/${id}/estado`, {
    method: 'PUT',
    headers: headersJson(),
    body: JSON.stringify(nuevoEstado),
  });

  await throwIfNotOk(response, 'Error al actualizar estado');
  return await response.json();
}

// 6. RECLAMOS
export async function getReclamos(currentUser) {
  const response = await fetch(RECLAMOS_BASE_URL, {
    headers: headersUsuarioSinBody(currentUser),
  });

  await throwIfNotOk(response, 'Error al obtener reclamos');
  return await response.json();
}

export async function createReclamo(reclamo, currentUser) {
  const response = await fetch(RECLAMOS_BASE_URL, {
    method: 'POST',
    headers: headersUsuario(currentUser),
    body: JSON.stringify(reclamo),
  });

  await throwIfNotOk(response, 'Error al crear reclamo');
  return await response.json();
}

export async function updateReclamoEstado(id, nuevoEstado, currentUser) {
  const response = await fetch(`${RECLAMOS_BASE_URL}/${id}/estado`, {
    method: 'PUT',
    headers: headersUsuario(currentUser),
    body: JSON.stringify(nuevoEstado),
  });

  await throwIfNotOk(response, 'Error al actualizar reclamo');
  return await response.json();
}

export async function deleteReclamo(id, currentUser) {
  const response = await fetch(`${RECLAMOS_BASE_URL}/${id}`, {
    method: 'DELETE',
    headers: headersUsuarioSinBody(currentUser),
  });

  await throwIfNotOk(response, 'Error al eliminar reclamo');

  const txt = await response.text().catch(() => '');
  if (txt.trim() === '') return true;
  if (txt.trim() === 'true') return true;
  if (txt.trim() === 'false') return false;

  try {
    const parsed = JSON.parse(txt);
    return typeof parsed === 'boolean' ? parsed : !!parsed;
  } catch {
    return false;
  }
}
