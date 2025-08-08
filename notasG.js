// // const url = "https://opensheet.elk.sh/1AjFxZGgplX6ipPVRGdTa6Py-V-kLScafpOoJAw8Jrqg/Hoja 1";
// const contenedornotasG = document.querySelector("#botonesnotasG");
// const textonotasG = document.getElementById("textonotasG");

// const contenido = JSON.parse(localStorage.getItem("misNotas") || "[]");
// const fecha = JSON.parse(localStorage.getItem("fecha") || "[]");

// // Si no hay notas, no hacer nada
// if (contenido.length === 0) {
//   textonotasG.textContent = "No hay notas guardadas.";
// } else {
//   contenido.forEach((nota, indice) => {
//     const detail = document.createElement("details");
//     detail.classList.add('notas');

//     const summary = document.createElement("summary");
//     // Aseguramos que fecha[indice] exista, sino ponemos índice o vacío
//     summary.textContent = "Nota " + (fecha[indice] || (indice + 1));

//     const p = document.createElement("p");
//     p.textContent = nota;

//     detail.appendChild(summary);
//     detail.appendChild(p);
//     // detail.appendChild(b); // Elimino porque 'b' no está definido

//     contenedornotasG.appendChild(detail);
//   });
// }

function mostrarNotasEnParrafo() {
    const textonotasG = document.getElementById("textonotasG");
    textonotasG.classList.add("hidden");
    const contenedornotasG = document.querySelector("#botonesnotasG");
    const notasGuardadas = JSON.parse(localStorage.getItem("misNotas") || "[]");

    if (notasGuardadas.length === 0) {
        textonotasG.textContent = "No hay notas guardadas";
        return;
    }
    notasGuardadas.forEach((nota) => {
        const boton = document.createElement("button");
        boton.textContent = `Nota del ${nota.fecha}`;
        contenedornotasG.appendChild(boton);
        boton.addEventListener('click',() => {
            textonotasG.classList.remove("hidden");
            textonotasG.textContent=nota.texto;
        });
    });
const textoCompleto = notasGuardadas
//     .map(nota => `${nota.fecha} - ${nota.texto}`)
//     .join("\n");

//   textonotasG.textContent = textoCompleto;
}

document.addEventListener("DOMContentLoaded", mostrarNotasEnParrafo);

