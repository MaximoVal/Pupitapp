// Funcion para el inicio---------------------------------------------------------
document.addEventListener("DOMContentLoaded", function () {
  
  const boton = document.getElementById("entrar");
  const entrada= document.getElementById("animacionIndex")

  boton.addEventListener("click", function () {
    entrada.style.display="block";
    entrada.classList.add("animacionEntrada");

    setTimeout(() => {
      window.location.href = "menu.html"; // redirige después del desvanecimiento
    }, 200); // mismo tiempo que la transición CSS (1s)
  });
});
//-------------------------------------------------------------------------


// Funcion para las cartas----------------------------------------------------
document.addEventListener("DOMContentLoaded", () => {
  const cartas = document.querySelectorAll(".carta"); // ahora con class
  const salida = document.getElementById("animacion")
  salida.style.display="block"
  salida.classList.add("animacionSalida");
  setTimeout(()=>
  {
    salida.style.display="none";
  },500)

  cartas.forEach((carta) => {
    carta.addEventListener("click", () => {
      carta.classList.add("expandir");

      let destino = "#"; // Por defecto
      if (carta.classList.contains("feliz")) destino = "feliz.html";
      if (carta.classList.contains("triste")) destino = "triste.html";
      if (carta.classList.contains("enojada")) destino = "enojada.html";
      if (carta.classList.contains("extraña")) destino = "extraña.html";
      if (carta.classList.contains("extra")) destino = "extra.html";
      if (carta.classList.contains("nota")) destino = "notasG.html";

      setTimeout(() => {
        window.location.href = destino;
      }, 300); // Espera a que termine la transición
    });
  });
});
//----------------------------------------------------------------------

//Funcion para los botones-----------------------------------------------
document.addEventListener("DOMContentLoaded", () => {
  const texto = document.getElementById("textoMostrado");
  texto.classList.add("hidden")
  const btnExtra= document.getElementById("botonSemanal")
  btnExtra.addEventListener('click',()=>
    {
      texto.classList.remove("hidden")
      let numero;
      numero=Math.floor(Math.random() * 6);
      if (numero===0)
      {
        texto.textContent='Vale por un chocolate'
      }
      else if(numero===1)
      {
        texto.textContent='Vale por un heladito'
      }
      else if(numero===2){
        texto.textContent='Vale por unos buenos masajes'
      }
      else if(numero===3){
        texto.textContent='Vos elegis el premio, omgg'
      }
      else if(numero===4)
      {
        texto.textContent='Noche de La ley y el Orden'
      }
      else if(numero===5)
      {
        texto.textContent='Vale por un postrecito'
      }
  });
  
});
const volver=document.querySelector(".volver");
volver.addEventListener("click", () => {
    window.location.href = "menu.html";
  });
//-------------------------------------------------------------

/*------------------------Animacion entrada-------------------------------*/

