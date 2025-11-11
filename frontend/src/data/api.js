const API_BASE_URL = 'http://localhost:8080/api/perfil';

export async function loginRequest(email, password) {
  const response = await fetch(`${API_BASE_URL}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      correo: email,
      clave: password,
    }),
  });

  if (!response.ok) {
    const texto = await response.text().catch(() => '');
    throw new Error(texto || 'Error al iniciar sesión');
  }

  return await response.json();
}

export async function getPerfiles() {
  const response = await fetch(`${API_BASE_URL}/todos`);
  if (!response.ok) {
    throw new Error('Error al obtener perfiles');
  }
  return await response.json();
}

export async function createPerfil(nuevoPerfil) {
  const response = await fetch(`${API_BASE_URL}/registrar`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(nuevoPerfil),
  });

  if (!response.ok) {
    const textoError = await response.text().catch(() => '');
    throw new Error(textoError || 'Error HTTP al registrar perfil');
  }

  await response.json();
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
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const textoError = await response.text().catch(() => '');
    throw new Error(textoError || 'Error HTTP al registrar cliente');
  }

  await response.json();
  return await loginRequest(payload.correo, payload.clave);
}

const INVENTARIO_BASE_URL = 'http://localhost:8080/api/inventario';

export async function getProductos() {
  const response = await fetch(`${INVENTARIO_BASE_URL}/productos`);
  if (!response.ok) {
    throw new Error('Error al obtener productos del inventario');
  }
  return await response.json();
}

export async function createProducto(producto) {
  const response = await fetch(`${INVENTARIO_BASE_URL}/productos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(producto),
  });

  if (!response.ok) {
    const textoError = await response.text().catch(() => '');
    throw new Error(textoError || 'Error HTTP al registrar el producto');
  }

  const ok = await response.json();
  if (!ok) {
    throw new Error('El backend no pudo registrar el producto');
  }

  return ok;
}

const PEDIDOS_BASE_URL = 'http://localhost:8080/api/pedidos';

export async function createPedido(pedido) {
  const response = await fetch(PEDIDOS_BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(pedido),
  });

  if (!response.ok) {
    const textoError = await response.text().catch(() => '');
    throw new Error(textoError || 'Error HTTP al registrar el pedido');
  }

  const ok = await response.json();
  if (!ok) {
    throw new Error('El backend no pudo registrar el pedido');
  }

  return ok;
}

export async function getPedidos() {
  const response = await fetch(PEDIDOS_BASE_URL);
  if (!response.ok) {
    throw new Error('Error al obtener pedidos');
  }
  return await response.json();
}

const FACTURAS_BASE_URL = 'http://localhost:8080/api/facturas';

export async function getFacturas() {
  const response = await fetch(FACTURAS_BASE_URL);
  if (!response.ok) {
    throw new Error('Error al obtener facturas');
  }
  return await response.json();
}
export async function createFactura(idPedido) {
  const payload = { idPedido };

  const response = await fetch(FACTURAS_BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const textoError = await response.text().catch(() => '');
    throw new Error(textoError || 'Error HTTP al registrar la factura');
  }

  const ok = await response.json();
  if (!ok) {
    throw new Error('El backend no pudo registrar la factura');
  }

  return ok;
}
const RECLAMOS_BASE_URL = 'http://localhost:8080/api/reclamos';

export async function getReclamos() {
  const response = await fetch(RECLAMOS_BASE_URL);
  if (!response.ok) {
    throw new Error('Error al obtener reclamos');
  }
  return await response.json();
}

export async function createReclamo(reclamo) {
  const response = await fetch(RECLAMOS_BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(reclamo),
  });

  if (!response.ok) {
    const textoError = await response.text().catch(() => '');
    throw new Error(textoError || 'Error HTTP al registrar reclamo');
  }

  const ok = await response.json();
  return ok;
}

export async function updateReclamoEstado(id, nuevoEstado) {
  const response = await fetch(`${RECLAMOS_BASE_URL}/${id}/estado`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(nuevoEstado), // "EN_PROCESO" o "CERRADO"
  });

  if (!response.ok) {
    const textoError = await response.text().catch(() => '');
    throw new Error(textoError || 'Error HTTP al actualizar estado de reclamo');
  }

  const ok = await response.json();
  return ok;
}
