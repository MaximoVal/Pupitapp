const url = "https://opensheet.elk.sh/1AjFxZGgplX6ipPVRGdTa6Py-V-kLScafpOoJAw8Jrqg/Hoja 1";
const contenedorEnojada= document.querySelector("#botonesEnojada");
const textoEnojada = document.getElementById("textoEnojada");
textoEnojada.classList.add("hidden");
async function cargarDatosEnojada() {
  try {
    const res = await fetch(url);
    const data = await res.json();
    contenedorEnojada.innerHTML = "";
    let contadorEn=0
    data.forEach(categoria => {
      if (categoria.Enojada && categoria.Enojada.trim() !== "") {
        contadorEn++;
        const b = document.createElement("button");
        b.textContent = contadorEn;
        b.classList.add("enojada");
        b.addEventListener('click',()=>{
          textoEnojada.classList.remove("hidden")
          textoEnojada.textContent=categoria.Enojada;
        })
        contenedorEnojada.appendChild(b);
  }
    });
   

  } catch (error) {
    console.error("Error al cargar los nombres:", error);
    contenedor.innerHTML = "<p>No se pudieron cargar los datos.</p>";
  }}

cargarDatosEnojada();
setInterval(cargarDatosEnojada,60000);


