const url = "https://opensheet.elk.sh/1AjFxZGgplX6ipPVRGdTa6Py-V-kLScafpOoJAw8Jrqg/Hoja 1";
const contenedorTriste= document.querySelector("#botonesTriste");
const textoTriste = document.getElementById("textoTriste");
textoTriste.classList.add("hidden");

async function cargarDatosTriste() {
  try {
    const res = await fetch(url);
    const data = await res.json();
    let contadorT=0;
    contenedorTriste.innerHTML = "";
    data.forEach(categoria => {
      if (categoria.Triste && categoria.Triste.trim() !== "") {
        contadorT++;
        let numeroBoton = contadorT;
        const b2 = document.createElement("button");
        b2.textContent = contadorT;
        b2.classList.add("triste");
        b2.addEventListener('click',()=>{
          textoTriste.classList.remove("hidden");
          if((numeroBoton!==3)){
            textoTriste.textContent=categoria.Triste;
          }
          else 
          {
            textoTriste.innerHTML=categoria.Triste + '<br><img src="azu14febr.jpg"  style="width: 300px;"> <p> Tu pupito </p>';
            
          }
        })
        contenedorTriste.appendChild(b2);
  }
    });
   

  } catch (error) {
    console.error("Error al cargar los nombres:", error);
    contenedor.innerHTML = "<p>No se pudieron cargar los datos.</p>";
  }}


cargarDatosTriste();
setInterval(cargarDatosTriste,60000)