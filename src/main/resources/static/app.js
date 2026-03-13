// ====== Estado global ======
const state = {
  paginaActual: 0,
  tamPagina: 5,
  totalPaginas: 0,
  totalElementos: 0,
  editandoId: null,
  eliminandoId: null,
  filtroTimer: null
};

// ====== Init ======
document.addEventListener('DOMContentLoaded', () => {
  mostrarLista();
});

// ====== Navegación ======
function mostrarLista() {
  document.getElementById('view-list').classList.add('active');
  document.getElementById('view-form').classList.remove('active');
  cargarContactos();
}

function mostrarFormulario(contacto = null) {
  document.getElementById('view-list').classList.remove('active');
  document.getElementById('view-form').classList.add('active');
  limpiarFormulario();

  if (contacto) {
    state.editandoId = contacto.id;
    document.getElementById('form-titulo').textContent = 'Editar Contacto';
    document.getElementById('inp-nombre').value = contacto.nombre;
    document.getElementById('inp-apellidos').value = contacto.apellidos;
    document.getElementById('inp-correo').value = contacto.correo;
    document.getElementById('inp-telefono').value = contacto.telefono;
    document.getElementById('inp-cp').value = contacto.codigoPostal;
    document.getElementById('inp-fecha').value = contacto.fechaNacimiento;
    calcularEdad();
  } else {
    state.editandoId = null;
    document.getElementById('form-titulo').textContent = 'Nuevo Contacto';
  }
}

function limpiarFormulario() {
  ['nombre','apellidos','correo','telefono','cp','fecha','foto'].forEach(id => {
    const el = document.getElementById('inp-' + id);
    if (el) el.value = '';
  });
  ['nombre','apellidos','correo','telefono','codigoPostal','fechaNacimiento','foto'].forEach(campo => {
    const err = document.getElementById('err-' + campo);
    if (err) err.textContent = '';
    const inp = document.getElementById('inp-' + campo.replace('codigoPostal','cp').replace('fechaNacimiento','fecha'));
    if (inp) inp.classList.remove('error');
  });
  document.getElementById('edad-info').textContent = 'Edad: —';
}

// ====== Cargar contactos ======
async function cargarContactos(pagina = 0) {
  state.paginaActual = pagina;

  const nombre = document.getElementById('f-nombre')?.value.trim() || '';
  const correo = document.getElementById('f-correo')?.value.trim() || '';
  const telefono = document.getElementById('f-telefono')?.value.trim() || '';
  const cp = document.getElementById('f-cp')?.value.trim() || '';
  const fecha = document.getElementById('f-fecha')?.value || '';

  const params = new URLSearchParams({
    page: state.paginaActual,
    size: state.tamPagina
  });
  if (nombre) params.set('nombre', nombre);
  if (correo) params.set('correo', correo);
  if (telefono) params.set('telefono', telefono);
  if (cp) params.set('codigoPostal', cp);
  if (fecha) params.set('fechaNacimiento', fecha);

  try {
    const resp = await fetch(`/api/contactos?${params}`);
    if (!resp.ok) throw new Error('Error al cargar contactos');
    const data = await resp.json();

    state.totalPaginas = data.totalPages;
    state.totalElementos = data.totalElements;

    renderTabla(data.content);
    renderPaginacion();
  } catch (e) {
    mostrarToast('Error al cargar contactos: ' + e.message, 'error');
  }
}

function filtrar() {
  clearTimeout(state.filtroTimer);
  state.filtroTimer = setTimeout(() => cargarContactos(0), 350);
}

// ====== Render tabla ======
function renderTabla(contactos) {
  const tbody = document.getElementById('tabla-body');
  if (!contactos.length) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:24px;color:#999;">No se encontraron contactos</td></tr>`;
    return;
  }
  tbody.innerHTML = contactos.map(c => `
    <tr>
      <td>
        <div class="foto-thumb">
          ${c.fotoUrl
            ? `<img src="${c.fotoUrl}" alt="${escapar(c.nombre)}" onerror="this.parentElement.innerHTML='👤'"/>`
            : '👤'}
        </div>
      </td>
      <td><strong>${escapar(c.nombre)} ${escapar(c.apellidos)}</strong></td>
      <td>${escapar(c.correo)}</td>
      <td>${escapar(c.telefono)}</td>
      <td>${escapar(c.codigoPostal)}</td>
      <td>${c.fechaNacimiento}</td>
      <td>
        <button class="btn btn-edit btn-sm" onclick="editarContacto(${c.id})">✏️</button>
        <button class="btn btn-delete btn-sm" onclick="pedirEliminar(${c.id})">🗑️</button>
      </td>
    </tr>
  `).join('');
}

// Prevenir XSS al insertar texto en el DOM
function escapar(texto) {
  const div = document.createElement('div');
  div.appendChild(document.createTextNode(String(texto)));
  return div.innerHTML;
}

// ====== Paginación ======
function renderPaginacion() {
  const info = document.getElementById('pag-info');
  const btns = document.getElementById('pag-btns');

  const inicio = state.paginaActual * state.tamPagina + 1;
  const fin = Math.min((state.paginaActual + 1) * state.tamPagina, state.totalElementos);
  info.textContent = state.totalElementos > 0
    ? `Mostrando ${inicio}-${fin} de ${state.totalElementos} registros (Página ${state.paginaActual + 1} de ${state.totalPaginas})`
    : 'Sin resultados';

  const paginas = [];
  paginas.push(`<button class="page-btn" onclick="cargarContactos(0)" ${state.paginaActual===0?'disabled':''}>« Primero</button>`);
  paginas.push(`<button class="page-btn" onclick="cargarContactos(${state.paginaActual-1})" ${state.paginaActual===0?'disabled':''}>‹</button>`);

  const rango = 2;
  for (let i = Math.max(0, state.paginaActual - rango); i <= Math.min(state.totalPaginas - 1, state.paginaActual + rango); i++) {
    paginas.push(`<button class="page-btn ${i===state.paginaActual?'active':''}" onclick="cargarContactos(${i})">${i + 1}</button>`);
  }

  paginas.push(`<button class="page-btn" onclick="cargarContactos(${state.paginaActual+1})" ${state.paginaActual>=state.totalPaginas-1?'disabled':''}>›</button>`);
  paginas.push(`<button class="page-btn" onclick="cargarContactos(${state.totalPaginas-1})" ${state.paginaActual>=state.totalPaginas-1?'disabled':''}>Último »</button>`);
  btns.innerHTML = paginas.join('');
}

// ====== CRUD ======
async function editarContacto(id) {
  try {
    const resp = await fetch(`/api/contactos?page=0&size=1000`);
    const data = await resp.json();
    const contacto = data.content.find(c => c.id === id);
    if (contacto) mostrarFormulario(contacto);
  } catch {
    mostrarToast('No se pudo cargar el contacto', 'error');
  }
}

function pedirEliminar(id) {
  state.eliminandoId = id;
  document.getElementById('modal-overlay').classList.add('show');
}

function cerrarModal() {
  state.eliminandoId = null;
  document.getElementById('modal-overlay').classList.remove('show');
}

async function confirmarEliminar() {
  if (!state.eliminandoId) return;
  cerrarModal();
  try {
    const resp = await fetch(`/api/contactos/${state.eliminandoId}`, { method: 'DELETE' });
    if (!resp.ok) throw new Error('No se pudo eliminar');
    mostrarToast('Contacto eliminado', 'success');
    cargarContactos(state.paginaActual);
  } catch (e) {
    mostrarToast(e.message, 'error');
  }
}

async function guardar() {
  if (!validarFormulario()) return;

  const btn = document.getElementById('btn-guardar');
  btn.disabled = true;
  btn.textContent = 'Guardando...';

  const fd = new FormData();
  fd.append('nombre', document.getElementById('inp-nombre').value.trim());
  fd.append('apellidos', document.getElementById('inp-apellidos').value.trim());
  fd.append('correo', document.getElementById('inp-correo').value.trim().toLowerCase());
  fd.append('telefono', document.getElementById('inp-telefono').value.trim());
  fd.append('codigoPostal', document.getElementById('inp-cp').value.trim());
  fd.append('fechaNacimiento', document.getElementById('inp-fecha').value);

  const foto = document.getElementById('inp-foto').files[0];
  if (foto) fd.append('foto', foto);

  const url = state.editandoId ? `/api/contactos/${state.editandoId}` : '/api/contactos';
  const method = state.editandoId ? 'PUT' : 'POST';

  try {
    const resp = await fetch(url, { method, body: fd });
    const data = await resp.json();

    if (!resp.ok) {
      if (data.errores) {
        Object.entries(data.errores).forEach(([campo, msg]) => {
          mostrarError(campo, msg);
        });
      } else {
        mostrarToast(data.mensaje || 'Error al guardar', 'error');
      }
      return;
    }

    mostrarToast(state.editandoId ? 'Contacto actualizado ✓' : 'Contacto creado ✓', 'success');
    mostrarLista();
  } catch (e) {
    mostrarToast('Error de red: ' + e.message, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Guardar Registro';
  }
}

// ====== Validaciones Frontend ======
const reglas = {
  nombre: { pattern: /^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s'-]+$/, min: 2, max: 100, msg: 'Solo letras, espacios, guiones y apóstrofes (2-100 caracteres)' },
  apellidos: { pattern: /^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s'-]+$/, min: 2, max: 100, msg: 'Solo letras, espacios, guiones y apóstrofes (2-100 caracteres)' },
  correo: { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, max: 150, msg: 'Ingresa un correo válido' },
  telefono: { pattern: /^\d{10}$/, msg: 'Exactamente 10 dígitos numéricos' },
  codigoPostal: { pattern: /^\d{4,10}$/, msg: 'Entre 4 y 10 dígitos numéricos' },
  fechaNacimiento: { msg: 'La fecha de nacimiento es obligatoria y debe ser pasada' }
};

function idPorCampo(campo) {
  const mapa = { codigoPostal: 'cp', fechaNacimiento: 'fecha' };
  return 'inp-' + (mapa[campo] || campo);
}

function validarCampo(el, campo) {
  const val = el.value.trim();
  const regla = reglas[campo];
  let error = '';

  if (!val) { error = 'Este campo es obligatorio'; }
  else if (regla.pattern && !regla.pattern.test(val)) { error = regla.msg; }
  else if (regla.min && val.length < regla.min) { error = `Mínimo ${regla.min} caracteres`; }
  else if (regla.max && val.length > regla.max) { error = `Máximo ${regla.max} caracteres`; }
  else if (campo === 'fechaNacimiento') {
    const hoy = new Date(); hoy.setHours(0,0,0,0);
    if (new Date(val) >= hoy) error = 'La fecha debe ser pasada';
  }

  mostrarError(campo, error);
  el.classList.toggle('error', !!error);
  return !error;
}

function mostrarError(campo, msg) {
  const el = document.getElementById('err-' + campo);
  if (el) el.textContent = msg || '';
  const inp = document.getElementById(idPorCampo(campo));
  if (inp) inp.classList.toggle('error', !!msg);
}

function validarFormulario() {
  const campos = [
    { id: 'inp-nombre', campo: 'nombre' },
    { id: 'inp-apellidos', campo: 'apellidos' },
    { id: 'inp-correo', campo: 'correo' },
    { id: 'inp-telefono', campo: 'telefono' },
    { id: 'inp-cp', campo: 'codigoPostal' },
    { id: 'inp-fecha', campo: 'fechaNacimiento' },
  ];
  let valido = true;
  campos.forEach(({ id, campo }) => {
    const el = document.getElementById(id);
    if (!validarCampo(el, campo)) valido = false;
  });

  // Validar foto si se seleccionó
  const foto = document.getElementById('inp-foto').files[0];
  if (foto) {
    if (!['image/jpeg','image/png','image/gif','image/webp'].includes(foto.type)) {
      document.getElementById('err-foto').textContent = 'Solo imágenes JPG, PNG, GIF o WEBP';
      valido = false;
    } else if (foto.size > 2 * 1024 * 1024) {
      document.getElementById('err-foto').textContent = 'La imagen no puede superar 2MB';
      valido = false;
    } else {
      document.getElementById('err-foto').textContent = '';
    }
  }
  return valido;
}

function validarFoto(el) {
  const foto = el.files[0];
  const err = document.getElementById('err-foto');
  if (!foto) { err.textContent = ''; return; }
  if (!['image/jpeg','image/png','image/gif','image/webp'].includes(foto.type)) {
    err.textContent = 'Solo imágenes JPG, PNG, GIF o WEBP';
    el.value = '';
  } else if (foto.size > 2 * 1024 * 1024) {
    err.textContent = 'La imagen no puede superar 2MB';
    el.value = '';
  } else {
    err.textContent = '';
  }
}

function soloNumeros(el) {
  el.value = el.value.replace(/\D/g, '');
}

function calcularEdad() {
  const val = document.getElementById('inp-fecha').value;
  const info = document.getElementById('edad-info');
  if (!val) { info.textContent = 'Edad: —'; return; }
  const hoy = new Date();
  const nac = new Date(val);
  let edad = hoy.getFullYear() - nac.getFullYear();
  const m = hoy.getMonth() - nac.getMonth();
  if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) edad--;
  info.textContent = edad >= 0 && edad < 150 ? `Edad: ${edad} años` : 'Edad: —';
}

// ====== Toast ======
let toastTimer;
function mostrarToast(msg, tipo = '') {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = 'show ' + tipo;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { t.className = ''; }, 3200);
}