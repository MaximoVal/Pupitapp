function mostrarMensaje() {
    // Mostrar el mensaje
    document.getElementById("mensaje").textContent = "¡Botón presionado! Espera 7 días para volver a presionar.";

    // Guardamos la fecha actual en localStorage
    let fechaActual = new Date();
    localStorage.setItem('fechaPresionBoton', fechaActual.toString());

    // Deshabilitar el botón
    document.getElementById("botonSemanal").disabled = true;

    // Llamamos a la función que verifica el tiempo transcurrido
    verificarTiempo();
}

// Función para verificar si han pasado 7 días y mostrar la cuenta regresiva
function verificarTiempo() {
    let fechaGuardada = localStorage.getItem('fechaPresionBoton');

    if (fechaGuardada) {
        // Convertir la fecha guardada en un objeto Date
        let fechaPresion = new Date(fechaGuardada);
        let fechaActual = new Date();

        // Calcular la diferencia en milisegundos
        let diferencia = fechaActual - fechaPresion;

        // Si han pasado 7 días o más, habilitar el botón
        if (diferencia >=  7 * 24 * 60 * 60 * 1000) {
            document.getElementById("botonSemanal").disabled = false;
            document.getElementById("mensaje").textContent = "¡Puedes volver a presionar el botón!";
        } else {
            document.getElementById("botonSemanal").disabled = true;
            let tiempoRestante = (7 * 24 * 60 * 60 * 1000) - diferencia;

            let diasRestantes = Math.floor(tiempoRestante / (1000 * 3600 * 24));
            let horasRestantes = Math.floor((tiempoRestante % (1000 * 3600 * 24)) / (1000 * 3600));
            let minutosRestantes = Math.floor((tiempoRestante % (1000 * 3600)) / (1000 * 60));
            let segundosRestantes = Math.floor((tiempoRestante % (1000 * 60)) / 1000);

            // Actualizar el mensaje con la cuenta regresiva
            document.getElementById("mensaje").textContent = `¡Esperando! Vuelve a intentarlo en ${diasRestantes}d ${horasRestantes}h ${minutosRestantes}m ${segundosRestantes}s.`;

            // Ejecutar la función cada segundo para actualizar la cuenta regresiva
            setTimeout(verificarTiempo, 1000);
        }
    }
}

// Ejecutamos la función de verificación cuando la página se cargue

window.onload = function() {
    verificarTiempo();
    if(diasRestantes==0){
        document.getElementById("botonSemanal").disabled = false;
    }
    
};
