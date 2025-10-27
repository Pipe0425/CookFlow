document.addEventListener('DOMContentLoaded', () => {

  // Inicializar modales de Bootstrap
  const editModalEl = document.getElementById('editModal');
  const passwordModalEl = document.getElementById('passwordModal');
  const editModal = editModalEl ? new bootstrap.Modal(editModalEl) : null;
  const passwordModal = passwordModalEl ? new bootstrap.Modal(passwordModalEl) : null;

  // Formularios
  const editForm = document.getElementById('editProfileForm') || (editModalEl ? editModalEl.querySelector('form') : null);
  const changePasswordForm = document.getElementById('changePasswordForm');

  // Helper: obtener perfil (PerfilDTO) -> contiene idUsuario
  async function fetchPerfil() {
    const res = await fetch('/api/usuarios/me', { credentials: 'same-origin' });
    if (!res.ok) throw new Error('No autenticado o no se pudo obtener perfil (status ' + res.status + ')');
    return await res.json();
  }

  // Helper: obtener usuario completo (UsuarioDTO) por id
  async function fetchUsuarioById(id) {
    if (!id) throw new Error('fetchUsuarioById: id no definido');
    const res = await fetch(`/api/usuarios/${id}`, { credentials: 'same-origin' });
    if (!res.ok) throw new Error('No se pudo obtener usuario por id (status ' + res.status + ')');
    return await res.json();
  }

  // --- Cargar datos en modal de edición cuando se abre ---
  if (editModalEl) {
    editModalEl.addEventListener('show.bs.modal', async () => {
      try {
        const perfil = await fetchPerfil();                 // trae idUsuario
        const usuario = await fetchUsuarioById(perfil.idUsuario); // trae objeto completo que espera el PUT

        // Rellenar campos (agrega más si incorporas más inputs)
        const nombreEl = document.getElementById('editNombre');
        const apellidoEl = document.getElementById('editApellido');
        if (nombreEl) nombreEl.value = usuario.nombre || '';
        if (apellidoEl) apellidoEl.value = usuario.apellido || '';

        // Si quieres exponer email (aunque en tu modal está readonly):
        const correoEl = document.getElementById('correo') || document.querySelector('input[name="email"]');
        if (correoEl && usuario.email) correoEl.value = usuario.email;

      } catch (err) {
        console.error('Error cargando modal de edición:', err);
        alert('No se pudieron cargar los datos del perfil. Intenta recargar la página.');
      }
    });
  }

  // --- Submit Edit Profile ---
  if (editForm) {
    editForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      try {
        // 1) obtenemos id actual
        const perfil = await fetchPerfil();
        const id = perfil.idUsuario;
        if (!id) throw new Error('ID de usuario no encontrado en perfil');

        // 2) obtenemos usuario completo (para mantener campos no editados)
        const usuario = await fetchUsuarioById(id);

        // 3) sobreescribimos los campos editables desde el form
        usuario.nombre = document.getElementById('editNombre')?.value || usuario.nombre;
        usuario.apellido = document.getElementById('editApellido')?.value || usuario.apellido;
        // Añade aquí otros campos editables si los tienes (telefono, documento, etc.)

        // 4) enviar PUT con el objeto completo
        const putRes = await fetch(`/api/usuarios/${id}`, {
          method: 'PUT',
          credentials: 'same-origin',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(usuario)
        });

        if (putRes.ok) {
          if (editModal) editModal.hide();
          // recargar la página para reflejar cambios server-rendered
          window.location.reload();
        } else {
          // intentar leer mensaje JSON o texto
          let msg = `Error al actualizar perfil (status ${putRes.status}).`;
          try {
            const j = await putRes.json();
            msg = j.message || JSON.stringify(j);
          } catch (_) {
            const t = await putRes.text().catch(()=>null);
            if (t) msg = t;
          }
          console.error('PUT /api/usuarios/:', msg);
          alert(msg);
        }
      } catch (err) {
        console.error('Error al actualizar perfil:', err);
        alert('Error al actualizar perfil. Revisa la consola.');
      }
    });
  }

  // --- Submit Change Password ---
  if (changePasswordForm) {
    changePasswordForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const errorDiv = document.getElementById('passwordError');
      const successDiv = document.getElementById('passwordSuccess');
      if (errorDiv) { errorDiv.style.display = 'none'; errorDiv.textContent = ''; }
      if (successDiv) { successDiv.style.display = 'none'; successDiv.textContent = ''; }

      const body = {
        currentPassword: document.getElementById('currentPassword')?.value,
        newPassword: document.getElementById('newPassword')?.value,
        confirmPassword: document.getElementById('confirmPassword')?.value
      };

      try {
        const res = await fetch('/api/usuarios/me/change-password', {
          method: 'POST',
          credentials: 'same-origin',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        });

        if (res.ok) {
          if (successDiv) {
            successDiv.textContent = '¡Contraseña cambiada exitosamente!';
            successDiv.style.display = 'block';
          } else {
            alert('Contraseña cambiada exitosamente.');
          }
          changePasswordForm.reset();
          setTimeout(() => {
            if (passwordModal) passwordModal.hide();
          }, 1400);
        } else {
          let msg = 'Error al cambiar la contraseña.';
          try {
            const j = await res.json();
            msg = j.message || JSON.stringify(j);
          } catch (_) {
            const t = await res.text().catch(()=>null);
            if (t) msg = t;
          }
          if (errorDiv) {
            errorDiv.textContent = msg;
            errorDiv.style.display = 'block';
          } else {
            alert(msg);
          }
        }
      } catch (err) {
        console.error('Error en change-password:', err);
        if (errorDiv) {
          errorDiv.textContent = 'Error de conexión. Intenta nuevamente.';
          errorDiv.style.display = 'block';
        } else {
          alert('Error de conexión. Intenta nuevamente.');
        }
      }
    });
  }

});
