const API_BASE_URL = 'http://localhost:8080/api/perfil';
const INVENTARIO_BASE_URL = 'http://localhost:8080/api/inventario';
const PEDIDOS_BASE_URL = 'http://localhost:8080/api/pedidos';
const FACTURAS_BASE_URL = 'http://localhost:8080/api/facturas';
const RECLAMOS_BASE_URL = 'http://localhost:8080/api/reclamos';

//  GESTIÓN DE USUARIOS (LOGIN/REGISTRO)

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
    throw new Error(textoError || 'Error al registrar perfil');
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
    throw new Error(textoError || 'Error al registrar cliente');
  }

  await response.json();
  return await loginRequest(payload.correo, payload.clave);
}

// 3. INVENTARIO (PRODUCTOS)

export async function getProductos() {
  const response = await fetch(`${INVENTARIO_BASE_URL}/productos`);
  if (!response.ok) {
    throw new Error('Error al obtener el catálogo');
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
    throw new Error('Error al registrar el producto');
  }

  return await response.json();
}

export async function updateProducto(producto) {
  const id = producto?.id;
  if (!id) throw new Error('El producto no tiene id para editar');

  const response = await fetch(`${INVENTARIO_BASE_URL}/productos/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(producto),
  });

  if (!response.ok) {
    const texto = await response.text().catch(() => '');
    throw new Error(texto || 'Error al editar el producto');
  }

  return await response.json(); 
}

export async function deleteProducto(id) {
  if (!id) throw new Error('El id es obligatorio para eliminar');

  const response = await fetch(`${INVENTARIO_BASE_URL}/productos/${id}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    const texto = await response.text().catch(() => '');
    throw new Error(texto || 'Error al eliminar el producto');
  }

  return await response.json(); 
}

// 4. PEDIDOS 

export async function createPedido(listaDePedidos) {
  const payload = Array.isArray(listaDePedidos) ? listaDePedidos : [listaDePedidos];

  const response = await fetch(PEDIDOS_BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const texto = await response.text().catch(() => '');
    throw new Error(texto || 'Error al procesar el pedido');
  }

  return await response.json(); 
}

export async function getPedidos() {
  const response = await fetch(PEDIDOS_BASE_URL);
  if (!response.ok) {
    throw new Error('Error al obtener pedidos');
  }
  return await response.json();
}

export async function updatePedido(pedido) {
  const response = await fetch(PEDIDOS_BASE_URL, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(pedido),
  });

  if (!response.ok) {
    const texto = await response.text().catch(() => '');
    throw new Error(texto || 'Error al actualizar el pedido');
  }

  return await response.json(); 
}

export async function deletePedido(id) {
  const response = await fetch(`${PEDIDOS_BASE_URL}/${id}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    const texto = await response.text().catch(() => '');
    throw new Error(texto || 'Error al eliminar el pedido');
  }

  return await response.json(); 
}

export async function deletePedidoPorCodigo(codigo) {
  const response = await fetch(`${PEDIDOS_BASE_URL}/codigo/${encodeURIComponent(codigo)}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    const texto = await response.text().catch(() => '');
    throw new Error(texto || 'Error al eliminar el pedido por código');
  }

  return await response.json();
}


// 5. FACTURAS

export async function getFacturas() {
  const response = await fetch(FACTURAS_BASE_URL);
  if (!response.ok) {
    throw new Error('Error al obtener facturas');
  }
  return await response.json();
}

export async function createFactura(datos) {
  
  const response = await fetch(FACTURAS_BASE_URL, {
    method: 'POST',
    headers: { 
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
    body: JSON.stringify(datos), 
  });

  if (!response.ok) {
    const texto = await response.text().catch(() => '');
    throw new Error(texto || 'Error al generar la factura');
  }

  return await response.json();
}

export async function deleteFactura(id) {
  const response = await fetch(`${FACTURAS_BASE_URL}/${id}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    throw new Error('Error al eliminar la factura');
  }

  return await response.json();
}

export async function updateFacturaEstado(id, nuevoEstado) {
  const response = await fetch(`${FACTURAS_BASE_URL}/${id}/estado`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(nuevoEstado), 
  });

  if (!response.ok) {
    throw new Error('Error al actualizar estado');
  }

  return await response.json();
}

// 6. RECLAMOS

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
    throw new Error('Error al crear reclamo');
  }

  return await response.json();
}

export async function updateReclamoEstado(id, nuevoEstado) {
  const response = await fetch(`${RECLAMOS_BASE_URL}/${id}/estado`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(nuevoEstado),
  });

  if (!response.ok) {
    throw new Error('Error al actualizar reclamo');
  }

  return await response.json();
}