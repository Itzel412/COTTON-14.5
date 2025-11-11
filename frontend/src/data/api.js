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
        const textoError = await response.text();
        throw new Error(textoError);
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
        const textoError = await response.text();

        throw new Error(textoError);
    }

    return await loginRequest(payload.correo, payload.clave);
}