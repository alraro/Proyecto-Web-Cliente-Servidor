# Guía de Referencia del Temario — Desarrollo Web para Clientes (JavaScript)

> **INSTRUCCIÓN CRÍTICA PARA EL AGENTE:** Esta guía define con exactitud los límites de conocimiento y las reglas de buenas prácticas del temario oficial de la asignatura. **Queda terminantemente prohibido utilizar, sugerir o mencionar cualquier concepto, API, biblioteca, sintaxis o patrón que no esté explícitamente recogido en este documento.** Ante la duda, la respuesta correcta es no usarlo.

---

## Índice

1. [Introducción a JavaScript](#1-introducción-a-javascript)
2. [Tipos, Variables y Operadores](#2-tipos-variables-y-operadores)
3. [Cadenas de Caracteres](#3-cadenas-de-caracteres)
4. [Sentencias de Control](#4-sentencias-de-control)
5. [Funciones](#5-funciones)
6. [Eventos](#6-eventos)
7. [Objetos](#7-objetos)
8. [Programación Orientada a Objetos y Clases](#8-programación-orientada-a-objetos-y-clases)
9. [Manipulación del DOM](#9-manipulación-del-dom)
10. [JavaScript Asíncrono](#10-javascript-asíncrono)
11. [JSON](#11-json)
12. [API Fetch y Peticiones de Red](#12-api-fetch-y-peticiones-de-red)
13. [Módulos JavaScript](#13-módulos-javascript)
14. [Introducción a las APIs Web](#14-introducción-a-las-apis-web)
15. [Buenas Prácticas y Lo que ESTÁ PROHIBIDO](#15-buenas-prácticas-y-lo-que-está-prohibido)

---

## 1. Introducción a JavaScript

JavaScript es un lenguaje de programación que permite crear contenido web que se actualiza dinámicamente, controlar multimedia y animar imágenes. Trabaja junto a HTML y CSS.

### Conceptos fundamentales presentes en el temario

- Definición de variables y constantes para almacenar estado.
- Definición de funciones para ejecutar acciones.
- Acceso de lectura y escritura al contenido HTML a través del DOM.
- Asignación de detectores de eventos (`addEventListener`).
- Definición de funciones anónimas.

### APIs del navegador reconocidas en el temario

Solo se pueden usar las siguientes APIs de navegador:

| API | Uso permitido |
|---|---|
| **DOM** (Document Object Model) | Acceder y manipular HTML y CSS |
| **Geolocalización** | Recuperar información geográfica del usuario |
| **Canvas / WebGL** | Gráficos animados 2D y 3D |
| **Audio y Vídeo** | Reproducción y captura de audio/vídeo |
| **API Fetch** | Peticiones HTTP a servidores remotos |
| **XMLHttpRequest** | Peticiones asíncronas (patrón AJAX antiguo — mencionada pero desaconsejada frente a Fetch) |
| **Window** (`setTimeout`, `setInterval`, etc.) | Temporizadores e intervalos |

### APIs de terceros mencionadas (solo como referencia, no se enseña su uso)

- API Bluesky, API Google Maps, API OpenStreetMap.

### Seguridad del navegador

- Cada pestaña ejecuta JavaScript en un entorno de ejecución independiente.
- Un script de JavaScript no puede leer archivos locales de la máquina.

### Orden de ejecución y carga de scripts

El código JavaScript se interpreta en el mismo orden en que aparece. Existen tres estrategias de carga permitidas:

1. **Colocar el `<script>` al final del `<body>`** — garantiza que el HTML ya está cargado.
2. **`<script type="module" src="...">`** — el archivo se carga después de todo el HTML automáticamente.
3. **`<script defer src="...">`** — para scripts externos que no son módulos.
4. **Envolver código interno en un listener `DOMContentLoaded`** — para código JavaScript interno.

```javascript
// Ejemplo con DOMContentLoaded
document.addEventListener("DOMContentLoaded", function() {
    const button = document.querySelector("button");
    button.addEventListener("click", actualizarNombre);
});
```

---

## 2. Tipos, Variables y Operadores

### Tipos primitivos

| Tipo | Descripción |
|---|---|
| `string` | Secuencia de caracteres |
| `number` | Número entero o decimal |
| `bigint` | Número entero grande |
| `boolean` | `true` o `false` |
| `undefined` | Sin valor asignado |
| `symbol` | Identificador único |
| `null` | Ausencia de valor |

Los tipos primitivos son **inmutables**. No tienen propiedades propias pero funcionan con auto-boxing.

### Tipos envoltura (Wrapper types)

```javascript
let t = Boolean(true);
let n = Number(34);
let s = String("cadena");

// Uso implícito (auto-boxing)
true.toString();
let n = 34;
n.toExponential();
```

### Declaración de variables

**Usar `let` para variables** (su ámbito es el bloque en que se declara):

```javascript
let variable = valor;
let testar = 0;
```

**Usar `const` para constantes** (el valor inicial no puede cambiar):

```javascript
const nombre = "Magdalena";
```

> ⚠️ **Prohibido usar `var`:** Aunque sigue funcionando, `var` tiene características problemáticas y está desaconsejado en el temario. El agente **no debe usar `var` ni sugerirlo** como práctica habitual.

> ⚠️ **Prohibido declarar variables implícitas (sin `let` o `const`):** Si no se declara una variable, se asume global, lo que es una mala práctica.

### Operadores de comparación

Usar siempre **`===`** (igualdad estricta, sin conversión de tipos implícita):

```javascript
if (x === 0) { ... }
```

> ⚠️ El uso de `==` no está desaconsejado explícitamente pero el temario usa `===` consistentemente como práctica correcta.

---

## 3. Cadenas de Caracteres

### Formas de declarar cadenas

```javascript
// Comillas dobles
const a = "Esta es una cadena";

// Comillas simples
const b = 'Esta es una cadena';

// Template literals (comillas invertidas) — permiten interpolación y multilínea
const nombre = "Magdalena";
const saludo = `Hola, ${nombre}`;
const porcentaje = `La nota es del ${(nota/max) * 100}%.`;
```

### Operaciones sobre cadenas permitidas en el temario

| Operación | Descripción |
|---|---|
| `.length` | Número de caracteres |
| `[indice]` / `.at(indice)` | Acceso a carácter concreto |
| `.includes(subcadena)` | Comprueba si contiene la subcadena |
| `.startsWith(sub)` | Comprueba si empieza con la subcadena |
| `.endsWith(sub)` | Comprueba si termina con la subcadena |
| `.indexOf(sub)` | Posición de la subcadena o `-1` |
| `.slice(inicio, fin)` | Extrae una subcadena |
| `.toLowerCase()` | Devuelve la cadena en minúsculas |
| `.toUpperCase()` | Devuelve la cadena en mayúsculas |
| `.replace(old, new)` | Sustituye la primera ocurrencia |
| `.replaceAll(old, new)` | Sustituye todas las ocurrencias |
| `.split(separador)` | Divide la cadena en un array |

### Concatenación

```javascript
const hola = "Hola, ";
const mundo = "mundo";
hola + mundo   // "Hola, mundo"
hola + 44      // "Hola, 44"
```

### Cadenas multilínea

```javascript
const nueva = `Un día finalmente sabes
lo que tienes que hacer, y lo haces`;

const nueva2 = "Primera línea\nSegunda línea";
```

---

## 4. Sentencias de Control

### Sentencias de selección

**`if / else if / else`:**

```javascript
if (hora < 12) {
    saludo = "Buenos días";
} else if (hora < 20) {
    saludo = "Buenas tardes";
} else {
    saludo = "Buenas noches";
}
```

**`switch`:**

```javascript
switch (mes) {
    case 1:
    case 3:
        numeroDias = 31;
        break;
    default:
        numeroDias = 28;
}
```

**Operador ternario (`?:`):**

```javascript
let resultado = condicion ? valorSiTrue : valorSiFalse;
```

### Sentencias de iteración

**Bucle `for` tradicional:**

```javascript
for (let i = 0; i < ciudades.length; i++) {
    console.log(ciudades[i]);
}
```

**Bucle `while`:**

```javascript
let i = 0;
while (!encontrada && i < ciudades.length) {
    if (ciudadBuscada === ciudades[i]) {
        encontrada = true;
    } else {
        i++;
    }
}
```

**Bucle `for...of`** (para iterables: arrays, cadenas, mapas, conjuntos):

```javascript
const ciudades = ["Málaga", "Córdoba", "Jaén"];
for (const c of ciudades) {
    console.log(c.toUpperCase());
}
```

### Operaciones sobre colecciones

**`map(función)`** — transforma cada elemento, devuelve nuevo array con el mismo número de elementos:

```javascript
let ciudadesMayusculas = ciudades.map(c => c.toUpperCase());
```

**`filter(función)`** — filtra elementos, devuelve un nuevo array con los que cumplen la condición:

```javascript
let ciudadesAcabanEnA = ciudades.filter(c => c.endsWith("a"));
```

---

## 5. Funciones

### Función básica

```javascript
function esMayorQue(x, y) {
    return x > y;
}
let resultado = esMayorQue(3, 2);
```

- No se declara el tipo de los parámetros ni del valor de retorno.
- La instrucción `return` es opcional.
- Los parámetros se pasan **por valor** para primitivos.
- Los objetos se pasan **por compartición** (referencia compartida): las modificaciones a propiedades del objeto son visibles fuera de la función.

### Parámetros con valor por defecto

```javascript
function saludoUsuario(nombre = "María") {
    console.log(`Hola ${nombre}`);
}
saludoUsuario();        // "Hola María"
saludoUsuario("Pepe"); // "Hola Pepe"
```

### Funciones anónimas

```javascript
(function () {
    alert("hola");
});
```

### Expresiones de función

```javascript
const cuadrado = function (numero) {
    return numero * numero;
};
```

### Funciones flecha (arrow functions)

```javascript
// Con cuerpo
textBox.addEventListener("keydown", (event) => {
    console.log(`Pulsaste "${event.key}".`);
});

// Con un solo parámetro y cuerpo de una línea (sin return explícito)
const doblados = [1, 2, 3].map(elem => elem * 2);
```

### Ámbito de variables

- Variables declaradas dentro de una función: **locales** a esa función.
- Variables declaradas fuera de una función: **globales** (accesibles en cualquier parte del código, incluyendo otros archivos JS).
- Las variables globales duplicadas en distintos archivos provocan conflictos de nombres.

---

## 6. Eventos

Un evento es una acción o acontecimiento que sucede sobre el sistema web (clic, tecla pulsada, carga de página, envío de formulario, etc.).

### Forma CORRECTA — `addEventListener`

```javascript
const btn = document.querySelector("button");
btn.addEventListener("click", () => {
    document.body.style.backgroundColor = "red";
});
```

Se pueden asociar múltiples manejadores al mismo evento sobre el mismo elemento.

Para eliminar un manejador:

```javascript
btn.removeEventListener("click", miManejador);
```

### Formas DESACONSEJADAS (prohibidas en buenas prácticas del temario)

> ⚠️ **Prohibido usar manejadores en línea (atributos HTML):**

```html
<!-- MAL: no hacer esto -->
<button onclick="crearParrafo()">¡Haz clic!</button>
```

> ⚠️ **Evitar la asignación directa a propiedades `on*`** (no permite múltiples manejadores):

```javascript
// DESACONSEJADO
button.onClick = () => { ... };
```

### Objeto evento

Cuando ocurre un evento, el sistema genera un objeto que se pasa como parámetro al manejador. Propiedades importantes:

- **`event.target`** — el elemento HTML sobre el que se produjo el evento.
- **`event.key`** — (en `KeyboardEvent`) la tecla pulsada.
- **`event.shiftKey`** — (en `KeyboardEvent`) si estaba activa la tecla Mayús.
- **`event.clientX` / `event.clientY`** — (en `MouseEvent`) coordenadas del ratón.
- **`event.movementX` / `event.movementY`** — (en `MouseEvent`) desplazamiento desde el último `mousemove`.

### Eventos de teclado reconocidos: `keydown`, `keyPressed`, `keyUp`
### Eventos de ratón reconocidos: `click`, `dblclick`, `mouseup`, `mousedown`, `mousemove`
### Otros eventos relevantes: `DOMContentLoaded`, `submit`, `loadend`

### Evitar comportamiento por defecto

```javascript
form.addEventListener("submit", (e) => {
    if (campo.value === "") {
        e.preventDefault(); // evita el envío por defecto del formulario
    }
});
```

---

## 7. Objetos

### Definición de objetos literales

```javascript
const persona = {
    nombre: ["Ana", "Blanco"],
    edad: 32,
    bio: function () {
        console.log(`${this.nombre[0]} tiene ${this.edad} años`);
    },
    presentacion() {
        console.log(`Hola, me llamo ${this.nombre[0]}.`);
    }
};
```

### Acceso a propiedades

```javascript
persona.edad;           // operador punto
persona["edad"];        // operador corchete
persona["nombre"][1];   // anidado con corchete

// Con variable como clave
const prop = "nombre";
persona[prop];
```

### Actualización y adición de propiedades

```javascript
persona.edad = 28;           // actualizar
persona.colorOjos = "negro"; // añadir nueva propiedad
```

### Variable `this`

`this` se refiere al objeto sobre el que se invoca el método. A diferencia de Java, **es obligatorio usar `this`** para acceder a propiedades del mismo objeto, nunca se puede omitir.

### Constructores de objetos (función constructora)

```javascript
function Persona(nombre) {
    this.nombre = nombre;
    this.presentacion = function () {
        console.log(`Hola, me llamo ${this.nombre}.`);
    };
}

const alicia = new Persona("Alicia");
alicia.presentacion();
```

---

## 8. Programación Orientada a Objetos y Clases

### Prototipos

- Todos los objetos tienen un prototipo (`__proto__`), que es también un objeto.
- La cadena de prototipos termina en `null`.
- Se puede consultar el prototipo con `Object.getPrototypeOf(objeto)`.
- Un objeto puede ocultar propiedades del prototipo definiéndolas directamente.

**Creación con `Object.create()`:**

```javascript
const prototipoPersona = {
    saludar() { console.log("Hola."); }
};
const carl = Object.create(prototipoPersona);
carl.saludar();
```

**Asignación de prototipo mediante `Object.assign()`:**

```javascript
Object.assign(Persona.prototype, prototipoPersona);
```

**Comprobar si una propiedad es propia (no heredada):**

```javascript
Object.hasOwn(objeto, "propiedad"); // true / false
```

### Clases

```javascript
class Persona {
    nombre;

    constructor(nombre) {
        this.nombre = nombre;
    }

    presentacion() {
        console.log(`Hola, me llamo ${this.nombre}`);
    }
}

let p = new Persona("Federico");
p.presentacion();
```

### Herencia

Herencia **simple** (solo una clase padre). Se usa `extends` y `super()`:

```javascript
class Profesor extends Persona {
    imparte;

    constructor(nombre, imparte) {
        super(nombre); // debe ser la primera instrucción
        this.imparte = imparte;
    }

    presentacion() {
        console.log(`Me llamo ${this.nombre} e imparto ${this.imparte}`);
    }
}
```

### Encapsulación — Propiedades privadas

Para hacer privada una propiedad o función se antepone `#`:

```javascript
class Estudiante extends Persona {
    #curso;

    constructor(nombre, curso) {
        super(nombre);
        this.#curso = curso;
    }

    puedeApuntarse() {
        return this.#curso > 1;
    }
}
```

Las propiedades privadas (`#propiedad`) no son accesibles desde fuera de la clase.

### Propiedades y funciones estáticas

```javascript
class Punto {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }

    static nombreMostrado = "Punto";

    static distancia(a, b) {
        const dx = a.x - b.x;
        const dy = a.y - b.y;
        return Math.hypot(dx, dy);
    }
}

console.log(Punto.nombreMostrado);      // acceso por clase
console.log(Punto.distancia(p1, p2));   // acceso por clase
```

Las funciones estáticas solo pueden hacer referencia a propiedades y funciones estáticas.

---

## 9. Manipulación del DOM

### Partes del navegador relevantes para JavaScript

| Parte | Objeto JS | Descripción |
|---|---|---|
| **Navigator** | `Navigator` | Estado e identidad del navegador |
| **Ventana** | `Window` | Pestaña actual, tamaño, almacenamiento local |
| **Documento** | `Document` | Página cargada — árbol DOM |

### El Modelo de Objetos del Documento (DOM)

El DOM es una representación en árbol de los elementos HTML de la página.

**Jerarquía de interfaces relevante:**

```
Node
├── Document → HTMLDocument
├── Element → HTMLElement
│   ├── HTMLButtonElement  <button>
│   ├── HTMLInputElement   <input>
│   ├── HTMLFormElement    <form>
│   ├── HTMLParagraphElement <p>
│   ├── HTMLDivElement     <div>
│   ├── HTMLAnchorElement  <a>
│   ├── HTMLImageElement   <img>
│   ├── HTMLHeadingElement <h1>–<h6>
│   ├── HTMLCanvasElement  <canvas>
│   ├── HTMLMediaElement → HTMLVideoElement / HTMLAudioElement
│   └── ... (ver temario completo)
```

### Interfaz `Node` — Atributos y métodos útiles

| Miembro | Descripción |
|---|---|
| `childNodes` | Lista de nodos descendientes (viva) |
| `firstChild` / `lastChild` | Primer/último hijo directo |
| `nextSibling` / `previousSibling` | Nodo siguiente/anterior |
| `textContent` | Contenido textual del nodo y sus descendientes |
| `parentNode` / `parentElement` | Nodo/elemento padre |
| `appendChild()` | Añade un hijo al nodo |
| `removeChild()` | Elimina un hijo |
| `replaceChild()` | Reemplaza un hijo |
| `cloneNode()` | Copia del nodo |
| `contains()` | Comprueba si un nodo es descendiente |

### Interfaz `Document` — Atributos y métodos útiles

| Miembro | Descripción |
|---|---|
| `document.activeElement` | Elemento con foco |
| `document.head` / `document.body` | Nodos `<head>` y `<body>` |
| `document.children` | Colección de elementos del documento |
| `document.images` / `document.forms` | Colecciones de imágenes/formularios |
| `document.createElement(tipo)` | Crea nuevo elemento HTML |
| `document.querySelector(selector)` | Selecciona primer elemento que coincide |
| `document.querySelectorAll(selector)` | Selecciona todos los elementos que coinciden |
| `document.getElementById(id)` | Selecciona elemento por su `id` |
| `document.append(nodos)` | Añade nodos detrás del último hijo |
| `document.replaceChildren(nodos)` | Sustituye todos los hijos |
| `document.getSelection()` | Texto seleccionado por el usuario |

### Manipulaciones DOM frecuentes

```javascript
// Seleccionar elementos
const btn = document.querySelector("button");
const paras = document.querySelectorAll(".resultParas p");
const campo = document.getElementById("nombrepr");

// Modificar texto
btn.textContent = "Nuevo texto";
elemento.style.backgroundColor = "white";
elemento.disabled = true;

// Crear y añadir elementos
const nuevoBtn = document.createElement("button");
nuevoBtn.textContent = "Empezar nueva partida";
document.body.append(nuevoBtn);

// Eliminar elementos
botonReiniciar.parentNode.removeChild(botonReiniciar);

// Recorrer colecciones
for (const para of paras) {
    para.textContent = "";
}
```

---

## 10. JavaScript Asíncrono

JavaScript es **monohebra** (single-threaded). La programación asíncrona permite ejecutar operaciones largas sin bloquear la página.

### Temporizadores

**`setTimeout`** — ejecuta una función una sola vez tras un tiempo:

```javascript
let idTimeout = setTimeout(() => {
    alert("Ha terminado el tiempo de espera");
}, 1000); // tiempo en milisegundos

clearTimeout(idTimeout); // cancelar antes de que se dispare
```

> ⚠️ El primer parámetro de `setTimeout` **no debe ser una cadena de código** (riesgo de inyección).

### Intervalos

**`setInterval`** — ejecuta una función periódicamente:

```javascript
const intervalID = setInterval(miCallback, 500, "Param1", "Param2");

function miCallback(a, b) {
    console.log(a);
}

clearInterval(intervalID); // detener el intervalo
```

### Retrollamadas (Callbacks)

Funciones que se pasan como parámetro para ser llamadas posteriormente. Eran el mecanismo principal de asincronía pero producen código difícil de mantener (callback hell) si se anidan. **Las APIs modernas usan Promesas en su lugar.**

### Promesas

Una **promesa** (`Promise`) es un objeto devuelto por una función asíncrona que representa el estado actual de la operación.

**Estados de una promesa:**

| Estado | Descripción |
|---|---|
| `pending` | Estado inicial, aún no completada |
| `fulfilled` | Operación con éxito → se llama al manejador `then()` |
| `rejected` | Operación con error → se llama al manejador `catch()` |

**Métodos de una promesa:**

| Método | Descripción |
|---|---|
| `.then(fn)` | Manejador de éxito; devuelve una nueva promesa |
| `.catch(fn)` | Manejador de error |
| `.finally(fn)` | Se ejecuta siempre (éxito o error); útil para limpieza |

**Encadenamiento de promesas:**

```javascript
fetch("url")
    .then(respuesta => {
        if (!respuesta.ok) {
            throw new Error(`HTTP error: ${respuesta.status}`);
        }
        return respuesta.json();
    })
    .then(datos => {
        console.log(datos[0].nombre);
    })
    .catch(error => {
        console.error(`Error: ${error}`);
    })
    .finally(() => {
        isLoading = false;
    });
```

**Combinación de múltiples promesas:**

```javascript
// Todas deben cumplirse
Promise.all([promesa1, promesa2, promesa3])
    .then(respuestas => { ... })
    .catch(error => { ... });

// Basta con que una se cumpla
Promise.any([promesa1, promesa2, promesa3])
    .then(respuesta => { ... });
```

### Funciones async / await

La palabra clave `async` convierte una función en asíncrona. Dentro de ella, `await` espera el resultado de una promesa de manera síncrona sin bloquear el resto de la página.

```javascript
async function fetchProductos() {
    try {
        const response = await fetch("url");
        if (!response.ok) {
            throw new Error(`HTTP error: ${response.status}`);
        }
        const data = await response.json();
        console.log(data[0].nombre);
    } catch (error) {
        console.error(`Error: ${error}`);
    }
}

fetchProductos();
```

- Toda función `async` devuelve una promesa.
- `await` solo se puede usar dentro de funciones `async` o en el nivel superior de un módulo.

---

## 11. JSON

**JSON** (JavaScript Object Notation) es un formato de texto para representar datos estructurados, usado habitualmente para transmitir datos entre clientes y servidores.

### Estructura JSON válida

```json
{
    "nombre": "Juana",
    "anyoNacimiento": 1516,
    "titulos": ["Reina de Aragón", "Condesa de Barcelona"]
}
```

### Restricciones del formato JSON (diferencias con JavaScript)

- Solo tipos serializables: cadenas, números, `true`, `false`, `null`, objetos y arrays.
- **No se permite:** `undefined`, `NaN`, `Infinity`, funciones, `Date`, `Set`, `Map`.
- Las claves de objetos **deben ir entre comillas dobles**.
- Las cadenas de caracteres deben ir entre comillas dobles (no simples).
- Los números en notación decimal.
- Sin coma al final de arrays u objetos.
- **Sin comentarios**.

### Conversión entre JSON y objetos JavaScript

```javascript
// JSON (cadena) → objeto JavaScript
const texto = '{"nombre": "Felipe", "apodo": "El Hermoso"}';
const objeto = JSON.parse(texto);

// Objeto JavaScript → JSON (cadena)
const rey = { nombre: "Carlos", apodo: "El Hechizado" };
const textoJSON = JSON.stringify(rey);
```

También se usa `response.json()` en respuestas de Fetch para convertir directamente.

---

## 12. API Fetch y Peticiones de Red

La **API Fetch** es la forma recomendada de hacer peticiones HTTP desde JavaScript. Sustituye a `XMLHttpRequest` (que usaba callbacks).

### Petición básica GET

```javascript
fetch("url")
    .then(respuesta => {
        if (!respuesta.ok) {
            throw new Error(`HTTP error: ${respuesta.status}`);
        }
        return respuesta.text();   // o .json() o .blob()
    })
    .then(datos => {
        elemento.textContent = datos;
    })
    .catch(error => {
        console.error(`Error: ${error}`);
    });
```

### Con async/await

```javascript
const respuesta = await fetch("https://example.org/datos");
```

### Elección del método HTTP

```javascript
const respuesta = await fetch("https://example.org/post", {
    method: "POST",
    // ...
});
```

Por defecto es `GET`. Otros valores reconocidos: `POST`, `PUT`, `HEAD`.

### Cuerpo de la petición (body)

Solo para `POST` y `PUT`. Tipos permitidos como cuerpo:

- `string`
- `ArrayBuffer`, `TypedArray`, `DataView`
- `Blob`, `File`
- `URLSearchParams`
- `FormData`
- `ReadableStream`

```javascript
// Enviando JSON
const respuesta = await fetch("url", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nombreusuario: "ejemplo" })
});

// Enviando datos de formulario URL-encoded
const respuesta = await fetch("url", {
    method: "POST",
    body: new URLSearchParams({ nombreusuario: "ejemplo", password: "pass" })
});
```

### Cabeceras (headers)

```javascript
// Opción 1: objeto literal
const respuesta = await fetch("url", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(datos)
});

// Opción 2: objeto Headers
const misCabeceras = new Headers();
misCabeceras.append("Content-Type", "application/json");
const respuesta = await fetch("url", {
    method: "POST",
    headers: misCabeceras,
    body: JSON.stringify(datos)
});
```

> ⚠️ **Nunca establecer `Content-Type` para `FormData`** — el navegador lo genera automáticamente con el boundary necesario.

### GET con parámetros en la URL

```javascript
const params = new URLSearchParams();
params.append("nombreusuario", "ejemplo");
const respuesta = await fetch(`https://example.org/login?${params}`);
```

### CORS (Cross-Origin Resource Sharing)

| Valor de `mode` | Comportamiento |
|---|---|
| `cors` (defecto) | Permite peticiones de origen cruzado con cabeceras CORS correctas |
| `same-origin` | Deshabilita completamente las peticiones de origen cruzado |
| `no-cors` | Deshabilita CORS, solo GET/POST/HEAD, respuesta opaca — uso muy limitado |

### Credenciales

| Valor de `credentials` | Comportamiento |
|---|---|
| `omit` | Nunca se envían credenciales |
| `same-origin` (defecto) | Solo se envían en peticiones del mismo origen |
| `include` | Siempre se envían (incluso cross-origin) |

### Cabeceras de respuesta CORS relevantes

- `Access-Control-Allow-Origin: *` — permite cualquier origen.
- `Access-Control-Allow-Origin: http://lugar-fiable.es` — origen específico.
- `Access-Control-Allow-Credentials: true` — permite credenciales cross-origin.

### Clonar una petición para reutilizarla

```javascript
const peticion1 = new Request("url", { method: "POST", body: JSON.stringify(datos) });
const peticion2 = peticion1.clone();

const respuesta1 = await fetch(peticion1);
const respuesta2 = await fetch(peticion2);
```

---

## 13. Módulos JavaScript

Los módulos permiten organizar el código en archivos separados con exportaciones e importaciones explícitas.

### Declarar un script como módulo en HTML

```html
<script type="module" src="main.js"></script>
```

Las sentencias `export` e `import` **solo se pueden usar en módulos**. Sin `type="module"`, el navegador dará error.

### Características especiales de los módulos

- Su carga se **pospone automáticamente** hasta después de cargar el HTML (no hace falta `defer`).
- El **modo estricto** se activa automáticamente.
- Se ejecutan **una sola vez** aunque se incluyan múltiples veces.
- Deben probarse en un **servidor HTTP** (no en local con `file://` por problemas CORS).
- Sus recursos no están disponibles en la consola del navegador.

### Exportación con nombre

```javascript
// cuadrado.js
export const nombre = "cuadrado";

export function dibujar(ctx, longitud, x, y, color) {
    ctx.fillStyle = color;
    ctx.fillRectangle(x, y, longitud, longitud);
    return { longitud, x, y, color };
}

// Alternativa: exportar al final
export { nombre, dibujar };
```

### Exportación por defecto (un único recurso por archivo)

```javascript
export default function dibujar(...) { ... }

// O al final:
export default dibujar;
```

### Importación

```javascript
// Importar con nombre
import { nombre, dibujar } from "./cuadrado.js";

// Importar exportación por defecto (sin llaves)
import dibujar from "./cuadrado.js";

// Importar todo como objeto módulo
import * as Cuadrado from "./cuadrado.js";
Cuadrado.dibujar(ctx, 50, 50, 100, "blue");
```

### Renombrar en la importación (recomendado para evitar conflictos)

```javascript
import {
    nombre as nombreCirculo,
    dibujar as dibujarCirculo
} from "./circulo.js";

import {
    nombre as nombreTriangulo,
    dibujar as dibujarTriangulo
} from "./triangulo.js";
```

### Renombrar en la exportación

```javascript
export { funcion1 as nuevaFuncion1, funcion2 as nuevaFuncion2 };
```

### Módulos y clases

```javascript
// cuadrado.js
class Cuadrado {
    constructor(ctx, listId, longitud, x, y, color) { ... }
    draw() { ... }
}
export { Cuadrado };

// main.js
import { Cuadrado } from "./cuadrado.js";
const c = new Cuadrado(ctx, id, 50, 50, 100, "blue");
```

### Agregación de módulos

Módulo intermedio que reexporta desde varios módulos:

```javascript
// figura.js (módulo agregador)
export { Cuadrado } from "./cuadrado.js";
export { Circulo } from "./circulo.js";
export { Triangulo } from "./triangulo.js";

// main.js
import { Cuadrado, Circulo, Triangulo } from "./figura.js";
```

### Importación dinámica

Carga un módulo solo cuando se necesita:

```javascript
import("./modulos/miModulo.js").then((Modulo) => {
    // Acceder a recursos con Modulo.recurso
});

// Ejemplo con evento
btnCirculo.addEventListener("click", () => {
    import("./modulos/cuadrado.js").then((Modulo) => {
        let c = new Modulo.cuadrado(ctx, 50, 50, 100, "azul");
        c.dibujar();
    });
});
```

### Importación de recursos no JavaScript

```javascript
import colores from "./colors.json" with { type: "json" };
import estilos from "./styles.css" with { type: "css" };
```

Es obligatorio indicar el tipo al importar archivos no JavaScript.

### `await` en el nivel superior de un módulo

Un módulo puede usar `await` en su nivel superior, actuando como una gran función asíncrona:

```javascript
// traerColores.js
const colores = fetch("../datos/colores.json").then(r => r.json());
export default await colores;
```

---

## 14. Introducción a las APIs Web

### ¿Qué es una API?

Conjunto de funciones que proporcionan acceso controlado a la funcionalidad de otro software. Ocultan la implementación interna y exponen solo lo necesario.

### Tipos de APIs en el cliente JavaScript

**APIs del navegador (integradas):**

| API | Función |
|---|---|
| DOM | Manipular HTML y CSS |
| Fetch | Peticiones HTTP, actualización dinámica de contenido (AJAX) |
| Canvas | Gráficos 2D programáticos en `<canvas>` |
| WebGL | Escenas 3D con iluminación y texturas |
| `window.requestAnimationFrame()` | Animaciones combinadas con Canvas/WebGL |
| HTMLMediaElement, Web Audio, WebRTC | Audio y vídeo (controles, subtítulos, captura de cámara) |
| Geolocation | Acceso al GPS del dispositivo |

**APIs externas (de terceros — solo mencionadas):**

- Google Maps, MapQuest (mapas).
- APIs de servicios web externos.

---

## 15. Buenas Prácticas y Lo que ESTÁ PROHIBIDO

Esta sección recoge de forma unificada todas las restricciones y recomendaciones explícitas del temario.

### ✅ OBLIGATORIO / RECOMENDADO

| Ámbito | Buena práctica |
|---|---|
| Declaración de variables | Usar `let` para variables, `const` para constantes |
| Comparaciones | Usar siempre `===` (igualdad estricta) |
| Eventos | Usar siempre `addEventListener` y `removeEventListener` |
| Scripts externos | Usar `<script defer>` o `<script type="module">` para garantizar la carga tras el HTML |
| Scripts internos | Envolver en `DOMContentLoaded` si están en `<head>` |
| Peticiones HTTP | Usar la **API Fetch** con promesas (o async/await) |
| Organización del código | Usar **módulos** para separar el código en archivos |
| Renombrado en módulos | Renombrar en la **importación** (deja el módulo exportador limpio) |
| Encapsulación | Declarar privadas (`#`) las propiedades internas de las clases |
| Herencia | Llamar a `super()` como **primera instrucción** del constructor |
| Errores en Fetch | Comprobar siempre `response.ok` y usar `.catch()` |
| Parámetros funciones | Definir valores por defecto cuando sea necesario |
| `this` en clases | Usar siempre `this.` para acceder a propiedades del objeto |

### ❌ PROHIBIDO / DESACONSEJADO

| Ámbito | Prohibición |
|---|---|
| Variables | **No usar `var`** |
| Variables | **No declarar variables implícitas** (sin `let`/`const`) |
| Eventos | **No usar manejadores en línea** (`onclick="..."` en el HTML) |
| Eventos | **Evitar la asignación directa a propiedades `on*`** (no permite múltiples manejadores) |
| Peticiones HTTP | **No usar `XMLHttpRequest` como primera opción** — está desaconsejado frente a Fetch |
| `setTimeout` | **No pasar una cadena de código como primer parámetro** (riesgo de inyección) |
| Módulos | **No usar `export`/`import` sin `type="module"`** en el `<script>` |
| Módulos | **No probar módulos con `file://`** (da errores CORS) |
| Módulos (JS en HTML) | **No usar `type="module"` y hacer pruebas solo en servidor HTTP** |
| JSON | **No incluir comentarios, `undefined`, `NaN`, funciones ni `Infinity`** en JSON |
| `Content-Type` | **No establecer `Content-Type` manualmente para `FormData`** |
| `CORS no-cors` | **No usar `mode: "no-cors"` salvo casos muy específicos** (respuesta opaca, no se puede leer nada) |
| Cualquier otro | **No usar ninguna tecnología, API, biblioteca o patrón que no esté explícitamente en este temario** |

### Resumen de lo que NO existe en el temario

Las siguientes tecnologías y conceptos no forman parte del temario y el agente **no debe usarlos, mencionarlos como solución ni sugerirlos**:

- Frameworks o bibliotecas externas (React, Vue, Angular, jQuery, etc.)
- TypeScript
- Node.js ni su ecosistema (npm, require, etc.)
- `sessionStorage` / `sessionStorage`
- `IndexedDB`
- Service Workers
- WebSockets
- Web Workers
- Decoradores
- Generadores (`function*`, `yield`)
- Iteradores personalizados
- Proxies y Reflect
- `Symbol` como identificador avanzado
- Expresiones regulares (RegExp) — no están desarrolladas en el temario
- CSS-in-JS
- Bundlers (Webpack, Vite, Rollup, etc.)
- `eval()` — mencionada implícitamente como práctica peligrosa

---

*Esta guía debe tratarse como la única fuente de verdad para cualquier respuesta relacionada con la asignatura. Si un concepto no aparece aquí, no está en el temario.*