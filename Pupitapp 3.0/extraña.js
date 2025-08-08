const url = "https://opensheet.elk.sh/1AjFxZGgplX6ipPVRGdTa6Py-V-kLScafpOoJAw8Jrqg/Hoja 1";
const contenedorExtraña= document.querySelector("#botonesExtraña");
const textoExtraña = document.getElementById("textoExtraña");
textoExtraña.classList.add("hidden");
const texto2="Algo bien quimico asi me entendes como te extraño,ajdajdaj te extraño mas yo amorcito, asi que ahora llamame asi hablamos y nos extrañamos un poquito menos";

async function cargarDatosExtraña() {
  try {
    const res = await fetch(url);
    const data = await res.json();
    let contadorE=0
    contenedorExtraña.innerHTML = "";
    data.forEach(categoria => {
      if (categoria.Extraña && categoria.Extraña.trim() !== "") {
        contadorE++;
        let numeroBoton = contadorE;
        const b = document.createElement("button");
        b.textContent = contadorE;
        b.classList.add("Extraña");
        b.addEventListener('click',()=>{
        textoExtraña.classList.remove("hidden")
        if(numeroBoton===2)
          {
            textoExtraña.innerHTML=categoria.Extraña + '<br><img src="Foto-mishu.jpg" style="width: 300px;"> <p>PD: fotito mientras hago esto y asi de triste estoy pq te extraño.Te amo</p>';
          }
        else if( numeroBoton===3)
          {
          textoExtraña.innerHTML=categoria.Extraña +  '<span style="color: #4caf50;">“Desde que no estás, siento que mi equilibrio químico se desplazó... y sólo volverá a su estado estable cuando estés conmigo.”</span>' + texto2;
          }
        else{
            textoExtraña.textContent=categoria.Extraña;
        }
        })
        contenedorExtraña.appendChild(b);
  }
    });
   

  } catch (error) {
    console.error("Error al cargar los nombres:", error);
    contenedor.innerHTML = "<p>No se pudieron cargar los datos.</p>";
  }}

cargarDatosExtraña();
setInterval(cargarDatosExtraña,60000)