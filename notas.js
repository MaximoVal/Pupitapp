const url = "https://opensheet.elk.sh/1AjFxZGgplX6ipPVRGdTa6Py-V-kLScafpOoJAw8Jrqg/Hoja 1";
const contenedorGeneral = document.getElementById("contenedorNotas");
const sinLeer = document.getElementById("sinLeer");
const leidos = document.getElementById("leidos");
const aside = document.getElementById("aside");
const salir = document.getElementById("salir");
/*-----Para Notas guardadas---*/
const notasG = document.getElementById("botonesnotasG")

let verif = 0;
salir.classList.add("hidden");
leidos.classList.add("hidden");
sinLeer.classList.add("hidden")
async function cargarDatosNotas() {
  try {
    const res = await fetch(url);
    const data = await res.json();

    // Obtener datos anteriores del localStorage
    const datosAnterioresJSON = localStorage.getItem("datosNotasAnteriores");
    const datosAnteriores = datosAnterioresJSON ? JSON.parse(datosAnterioresJSON) : null;

    // Comparar si hubo cambios
    const huboCambios = JSON.stringify(data) !== JSON.stringify(datosAnteriores);
    data.forEach(categoria => {
      if(!(categoria.Notas)){
        leidos.classList.remove("hidden")
      }
      });
    if (huboCambios) {
      leidos.classList.add("hidden");
      sinLeer.classList.remove("hidden")
    } else {
      sinLeer.classList.add("hidden")
      leidos.classList.remove("hidden");
    }
  

    // Guardar los datos nuevos en localStorage
    localStorage.setItem("datosNotasAnteriores", JSON.stringify(data));

    // Renderizar notas
    contenedorGeneral.innerHTML = ""; 
    let contadorN = 0;

    data.forEach(categoria => {
      if (categoria.Notas && categoria.Notas.trim() !== "") {
        contadorN++;
        const detail = document.createElement("details");
        detail.classList.add('notas');
        const summary = document.createElement("summary");
        summary.textContent = "Nota " + contadorN;
        const p = document.createElement("p");
        const b = document.createElement("button");
        b.style.cursor="pointer";
        b.textContent="Guardar";
        b.classList.add("botonGuardar")
        b.addEventListener('click', () => {
          const notasGuardadas = JSON.parse(localStorage.getItem("misNotas") || "[]");

          // Verifico que categoria y categoria.Notas existan
          const textoNota = categoria && categoria.Notas ? categoria.Notas : "Nota vacía";

          const fecha = new Date();
          const dia = fecha.getDate();
          const mes = fecha.getMonth() + 1;
          const anio = fecha.getFullYear();

          const fechaFormateada = `${dia}/${mes}/${anio}`;

          const nuevaNota = {
            texto: textoNota,
            fecha: fechaFormateada
          };

          notasGuardadas.push(nuevaNota);
          localStorage.setItem("misNotas", JSON.stringify(notasGuardadas));

          console.log("Notas guardadas:", notasGuardadas);
        });
        p.textContent = categoria.Notas;
        detail.appendChild(summary);
        detail.appendChild(p);
        detail.appendChild(b);
        contenedorGeneral.appendChild(detail);
      }
    });

  } catch (error) {
    console.error("Error al cargar las notas:", error);
    contenedorGeneral.innerHTML = "<p>No se pudieron cargar los datos.</p>";
  }
}

cargarDatosNotas();


// Manejadores de eventos
sinLeer.addEventListener('click', () => {
  contenedorGeneral.style.display = "flex";
  aside.style.backgroundColor = "#4C97D2";
  aside.style.borderRadius = "20px";
  sinLeer.classList.add("hidden");
  aside.classList.remove("mensajes");
  salir.classList.remove("hidden");
  verif = 1;
});

leidos.addEventListener('click', () => {
  contenedorGeneral.style.display = "flex";
  aside.style.backgroundColor = "#4C97D2";
  aside.style.borderRadius = "20px";
  leidos.classList.add("hidden");
  aside.classList.remove("mensajes");
  salir.classList.remove("hidden");
  verif = 1;
});

salir.addEventListener('click', () => {
  contenedorGeneral.style.display = "none";
  aside.style.backgroundColor = "rgb(118, 243, 69)";
  aside.classList.add("mensajes");
  aside.style.borderRadius = "50%";
  salir.classList.add("hidden");
  leidos.classList.remove("hidden");
});
