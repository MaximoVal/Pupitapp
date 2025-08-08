const url = "https://opensheet.elk.sh/1AjFxZGgplX6ipPVRGdTa6Py-V-kLScafpOoJAw8Jrqg/Hoja 1";
const contenedorFeliz= document.querySelector("#botonesFeliz");
const textoFeliz = document.getElementById("textoFeliz");
textoFeliz.classList.add("hidden");
async function cargarDatosFeliz() {
  try {
    const res = await fetch(url);
    const data = await res.json();
    let contadorF=0
    contenedorFeliz.innerHTML = "";
    data.forEach(categoria => {
      if (categoria.Feliz && categoria.Feliz.trim() !== "") {
        contadorF++;
        const b = document.createElement("button");
        b.textContent = contadorF;
        b.classList.add("feliz");
        b.addEventListener('click',()=>{
          textoFeliz.classList.remove("hidden")
          textoFeliz.textContent=categoria.Feliz;
        })
        contenedorFeliz.appendChild(b);
  }
    });
   

  } catch (error) {
    console.error("Error al cargar los nombres:", error);
    contenedor.innerHTML = "<p>No se pudieron cargar los datos.</p>";
  }}

cargarDatosFeliz();
setInterval(cargarDatosFeliz,60000);

