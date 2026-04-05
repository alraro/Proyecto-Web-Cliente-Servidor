incrementador = document.querySelector("#incrementar");

incrementador.addEventListener("click", () => {
    contador = document.querySelector("#counter");
    contador.innerHTML = parseInt(contador.innerHTML) + 1;
});