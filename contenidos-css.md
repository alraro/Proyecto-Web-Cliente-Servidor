# Guía CSS — Contenidos de la Asignatura

Esta guía define exactamente cómo aplicar CSS según lo visto en clase. Úsala como referencia canónica. No uses propiedades, valores ni técnicas que no aparezcan aquí.

---

## 1. Estructura de una regla CSS

```css
selector { propiedad: valor; propiedad: valor; }
```

- El **selector** indica a qué elementos HTML se aplica la regla.
- La **declaración** es el conjunto de pares `propiedad: valor` separados por `;`.
- Los comentarios se escriben con `/* comentario */`.

### Formas de incluir CSS en HTML

```html
<!-- 1. Hoja de estilo externa (RECOMENDADA) -->
<link rel="stylesheet" type="text/css" href="estilo.css">

<!-- 2. Hoja de estilo interna (solo en desarrollo o acceso restringido) -->
<style>
  p { color: purple; }
</style>

<!-- 3. Estilo en línea (solo en desarrollo o acceso restringido) -->
<span style="color: purple; font-weight: bold">texto</span>
```

> El estilo interno y en línea **no están recomendados** en general. Usar siempre hoja externa siempre que sea posible.

---

## 2. Selectores

### Tipos de selectores

| Selector | Sintaxis | Aplica a |
|---|---|---|
| Tipo de elemento | `p` | Todo `<p>` |
| Universal | `*` | Cualquier elemento |
| Clase | `.miClase` | Elementos con `class="miClase"` |
| ID | `#miId` | Elemento con `id="miId"` |
| Tipo + clase | `p.especial` | Solo `<p>` con `class="especial"` |
| Tipo + ID | `h1#datos` | Solo `<h1>` con `id="datos"` |
| Varias clases | `.notebox.warning` | Elemento con ambas clases a la vez |

### Selectores de atributo

```css
a[title]              /* <a> que tenga el atributo title */
a[href="https://..."] /* <a> con href exactamente igual al valor */
p[class~="special"]   /* <p> cuyo class incluye "special" en una lista */
div[lang|="es"]       /* <div> cuyo lang empieza por "es" seguido de guión */
a[href^="https"]      /* <a> cuyo href empieza por "https" */
a[href$=".pdf"]       /* <a> cuyo href termina en ".pdf" */
a[href*="example"]    /* <a> cuyo href contiene "example" */
```

### Agrupamiento de selectores

```css
h1, h2, h3 { color: green; } /* Aplica la misma regla a varios selectores */
```

### Combinadores

```css
body article p { color: red; }  /* Descendiente: cualquier <p> dentro de article dentro de body */
ul > li { color: red; }         /* Hijo directo: <li> hijo directo de <ul> */
h1 + p { font-weight: bold; }   /* Hermano siguiente: <p> inmediatamente después de <h1> */
h1 ~ p { color: white; }        /* Hermanos siguientes: todos los <p> hermanos de <h1> */
```

### Pseudoclases

```css
a:link    { color: #0000cc; }    /* Enlace no visitado */
a:visited { color: #ffcc33; }    /* Enlace visitado */
a:hover   { color: hotpink; }    /* Cursor encima del elemento */
a:active  { color: #ff0000; }    /* Elemento siendo pulsado */
p:focus   { background-color: green; } /* Elemento con el foco */
p:first-child { font-weight: bold; }   /* Primer hijo de su padre */
e:lang(es) { }                         /* Elemento marcado con idioma "es" */
```

### Pseudoelementos

```css
article p::first-line   { font-size: 120%; }  /* Primera línea del párrafo */
p::first-letter         { font-size: 200%; }  /* Primera letra del párrafo */
p::selection            { text-decoration: underline; } /* Texto seleccionado */

/* ::before y ::after: añaden contenido antes/después del elemento */
.box::before { content: "Texto añadido. "; background-color: yellow; }
.box::after  { content: " →"; }
```

### Combinación de pseudoclases y pseudoelementos

```css
article p:first-child::first-line { font-size: 120%; font-weight: bold; }
```

---

## 3. Valores CSS

### Unidades de longitud

| Unidad | Descripción |
|---|---|
| `px` | Píxel. Unidad absoluta. |
| `em` | Relativa al tamaño de fuente del elemento contenedor. |
| `rem` | Relativa al tamaño de fuente del elemento raíz `<html>`. |
| `%` | Porcentaje respecto al mismo valor del elemento padre. |
| `vw` | 1% del ancho del viewport. |
| `vh` | 1% del alto del viewport. |
| `pt` | Punto (1/72 de pulgada). Unidad física. |
| `cm`, `mm`, `in`, `pc` | Unidades físicas. |

> Unidades recomendadas en la mayoría de casos: `px`, `em`, `rem`.

### Colores

```css
color: #ff0000;          /* Hexadecimal completo */
color: #f00;             /* Hexadecimal corto (cada dígito se duplica) */
color: red;              /* Nombre de color */
color: rgb(255, 0, 0);   /* Función RGB */
color: rgba(255, 0, 0, 0.5); /* RGB con transparencia (0–1) */
```

Hay 16 colores básicos con nombre y ~150 extendidos. También se pueden usar `hsl()` y `hwb()`.

### Porcentajes

```css
li { font-size: 80%; } /* 80% del tamaño de fuente del padre */
```

### Cadenas y URLs

```css
background-image: url("imagen.jpg");
content: "Texto añadido";
```

### Posiciones

Valores posibles: `top`, `left`, `bottom`, `right`, `center` o longitudes.

```css
background-position: right 60px;
```

---

## 4. Propiedades de texto y fuente

### Color de texto

```css
color: red;
color: #333333;
color: rgb(0, 0, 0);
```

### Familia de fuente

```css
font-family: Arial, sans-serif;
font-family: "Times New Roman", serif;
```

Familias genéricas disponibles: `serif`, `sans-serif`, `monospace`, `cursive`, `fantasy`.

Fuentes seguras para la web: `Arial`, `Courier New`, `Georgia`, `Times New Roman`, `Trebuchet MS`, `Verdana`.

### Tamaño de fuente

```css
font-size: 16px;
font-size: 1.2em;
font-size: 1rem;
font-size: 80%;
font-size: 6vw;              /* Adaptativo al viewport */
font-size: calc(1.5rem + 4vw); /* Combinado con calc() */
```

### Estilo de fuente

```css
font-style: normal;
font-style: italic;
font-style: oblique;
```

### Grosor de fuente

```css
font-weight: normal;
font-weight: bold;
font-weight: lighter;
font-weight: bolder;
font-weight: 100; /* hasta 900, en múltiplos de 100 */
```

### Decoración de texto

```css
text-decoration: none;
text-decoration: underline;
text-decoration: overline;
text-decoration: line-through;
```

### Alineamiento de texto

```css
text-align: left;
text-align: right;
text-align: center;
text-align: justify;
```

### Transformación de texto

```css
text-transform: none;
text-transform: uppercase;
text-transform: lowercase;
text-transform: capitalize;
```

### Altura de línea

```css
line-height: 1.5;   /* Sin unidades: múltiplo del tamaño de fuente (recomendado) */
line-height: 2;
```

---

## 5. Modelo de cajas

Todos los elementos HTML tienen una caja con cuatro capas, de dentro hacia fuera:

1. **Content** (contenido): área donde se muestra el contenido. Tamaño: `width` y `height`.
2. **Padding** (relleno): espacio en blanco entre el contenido y el borde. Nunca negativo.
3. **Border** (borde): rodea contenido y relleno.
4. **Margin** (margen): espacio exterior alrededor de la caja. Puede ser negativo.

### width y height

```css
.box {
    width: 300px;
    height: 200px;
    width: 50%;    /* Porcentaje del contenedor */
}
```

Propiedades relacionadas:

```css
min-height: 100px;   /* Alto mínimo; crece si el contenido lo necesita */
max-width: 100%;     /* Ancho máximo; evita que supere el contenedor */
```

### Unidades de viewport

```css
width: 50vw;   /* 50% del ancho del viewport */
height: 100vh; /* 100% del alto del viewport */
```

### Margen (`margin`)

```css
margin: 20px;                   /* Los 4 lados iguales */
margin: 1em 2em;                /* Arriba/abajo | Derecha/izquierda */
margin: 1em 2em 3em;            /* Arriba | Derecha/izquierda | Abajo */
margin: 1em 2em 3em 2em;        /* Arriba | Derecha | Abajo | Izquierda */

margin-top: 10px;
margin-right: 10px;
margin-bottom: 10px;
margin-left: 10px;
```

> **Colapso de márgenes**: cuando dos márgenes se tocan, el resultado es el mayor de los dos (si ambos son positivos), el menor (si ambos son negativos) o su resta (si son de signo distinto).

### Relleno (`padding`)

```css
padding: 20px;
padding: 1em 2em;
padding: 1em 2em 3em;
padding: 1em 2em 3em 2em;

padding-top: 10px;
padding-right: 10px;
padding-bottom: 10px;
padding-left: 10px;
```

### Borde (`border`)

```css
border: 1px solid black;        /* Atajo: ancho estilo color */
border-top: thick solid red;
border-right: 2px dashed blue;
border-bottom: 1px dotted gray;
border-left: none;
```

Propiedades individuales:

```css
border-width: thin | medium | thick | <longitud>;
border-style: none | hidden | dotted | dashed | solid | double | groove | ridge | inset | outset;
border-color: red | transparent | <color>;

border-top-width: 2px;
```

### Radio de borde

```css
border-radius: 8px;
border-radius: 1em;
```

### Modelo de caja alternativo (`box-sizing`)

```css
box-sizing: border-box; /* El width incluye padding y border */
```

> Por defecto se usa el modelo estándar, donde `width` solo es el contenido. Con `border-box`, `width` es el tamaño visible total.

---

## 6. Fondo

```css
background-color: #f5f5f5;
background-color: yellow;

background-image: url("imagen.jpg");

background-repeat: no-repeat;
background-repeat: repeat;        /* Por defecto */
background-repeat: repeat-x;
background-repeat: repeat-y;

background-position: right 60px;
background-position: center;

background-size: cover;
background-size: contain;
background-size: 100px 200px;
```

---

## 7. Desbordamiento (`overflow`)

```css
overflow: visible; /* Por defecto: el contenido se muestra fuera de la caja */
overflow: hidden;  /* El contenido que desborda no se muestra */
overflow: scroll;  /* Siempre muestra barra de desplazamiento */
overflow: auto;    /* Barra de desplazamiento solo si hay desbordamiento */

overflow-x: hidden;
overflow-y: auto;
```

---

## 8. Display — tipo de visualización

```css
display: block;        /* El elemento ocupa toda la línea */
display: inline;       /* El elemento solo ocupa lo que necesita */
display: inline-block; /* Inline pero respeta width y height */
display: flex;         /* Contenedor flexible (Flexbox) */
display: grid;         /* Contenedor de cuadrícula (Grid) */
```

---

## 9. Flexbox

Se activa con `display: flex` en el contenedor.

```css
.contenedor {
    display: flex;

    /* Dirección del eje principal */
    flex-direction: row;           /* Por defecto: fila */
    flex-direction: column;
    flex-direction: row-reverse;
    flex-direction: column-reverse;

    /* Wrap: si los elementos no caben, pasan a la siguiente línea */
    flex-wrap: nowrap;   /* Por defecto */
    flex-wrap: wrap;

    /* Alineamiento en el eje principal (horizontal si flex-direction: row) */
    justify-content: flex-start;   /* Por defecto */
    justify-content: flex-end;
    justify-content: center;
    justify-content: space-between;
    justify-content: space-around;

    /* Alineamiento en el eje cruzado (vertical si flex-direction: row) */
    align-items: stretch;   /* Por defecto */
    align-items: center;
    align-items: flex-start;
    align-items: flex-end;

    /* Espacio entre elementos */
    gap: 16px;
}
```

Propiedad de los elementos hijos:

```css
.elemento { flex: 1; }  /* Proporción de espacio que ocupa */
.elemento { flex: 2; }  /* Ocupa el doble que los que tienen flex: 1 */
```

---

## 10. CSS Grid

Se activa con `display: grid` en el contenedor.

```css
.contenedor {
    display: grid;

    /* Definir columnas */
    grid-template-columns: 1fr 2fr 1fr;       /* 3 columnas con fracciones */
    grid-template-columns: repeat(3, 1fr);    /* Equivalente: 3 columnas iguales */
    grid-template-columns: 1cm 1cm 1cm;       /* Ancho fijo */
    grid-template-columns: repeat(auto-fit, minmax(230px, 1fr)); /* Tantas como quepan */

    /* Definir filas */
    grid-template-rows: repeat(3, 100px);
    grid-auto-rows: 100px;
    grid-auto-rows: minmax(100px, auto);  /* Altura mínima con crecimiento automático */

    /* Espaciado entre celdas */
    gap: 20px;
    column-gap: 5%;
}
```

Colocación de elementos hijos por líneas:

```css
.caja1 {
    grid-column-start: 1;
    grid-column-end: 2;
    grid-row-start: 1;
    grid-row-end: 4;
}

/* Versión compacta */
.caja1 {
    grid-column: 1 / 2;
    grid-row: 1 / 4;
}

/* O con grid-area: fila-inicio / columna-inicio / fila-fin / columna-fin */
.caja1 {
    grid-area: 1 / 1 / 4 / 2;
}
```

---

## 11. Posicionamiento

```css
position: static;   /* Por defecto: flujo normal */
position: relative; /* Flujo normal + desplazamiento respecto a su posición */
position: absolute; /* Sale del flujo; se posiciona respecto al ancestro posicionado */
position: fixed;    /* Sale del flujo; posición fija en el viewport (no se mueve con el scroll) */
position: sticky;   /* Relativo hasta que el scroll lo saca del viewport; luego fijo */
```

Propiedades de desplazamiento (se usan con `relative`, `absolute`, `fixed` y `sticky`):

```css
top: 30px;
right: 20px;
bottom: 10px;
left: 40px;
```

### Índice Z

```css
z-index: 10;  /* Mayor valor = encima de otros. Por defecto: 0 */
```

---

## 12. Flotabilidad (`float` y `clear`)

```css
float: none;          /* Sin flotabilidad */
float: left;          /* Flota a la izquierda; el contenido lo rodea */
float: right;         /* Flota a la derecha */
float: inline-start;
float: inline-end;

/* Detener el flotado */
clear: left;
clear: right;
clear: both;
```

---

## 13. Disposición multicolumna

```css
.contenedor {
    column-count: 3;         /* 3 columnas */
    /* column-width: 200px; */ /* O por ancho: tantas columnas como quepan */
    column-gap: 20px;
    column-rule: 1px solid gray; /* Línea separadora entre columnas */
}

.elemento {
    column-span: all;  /* El elemento se expande por todas las columnas */
    column-span: none; /* Permanece en su columna (por defecto) */
}

/* Evitar que un elemento se parta entre columnas */
.elemento {
    break-inside: avoid;
}
```

---

## 14. Diseño adaptativo (Responsive Design)

### Media queries

```css
@media screen and (width >= 600px) {
    .wrapper {
        display: flex;
    }
}

@media screen and (width >= 80rem) {
    .container {
        margin: 1em 2em;
    }
}

@media (width >= 1200px) {
    h1 {
        font-size: 4rem;
    }
}
```

### Imágenes y vídeos adaptativos

```css
img, picture, video {
    max-width: 100%;
}
```

### Texto adaptativo

```css
html { font-size: 1em; }
h1   { font-size: 2rem; }

/* Tamaño de fuente que varía con el viewport */
h1 { font-size: 6vw; }

/* Combinando tamaño fijo y variable (permite hacer zoom) */
h1 { font-size: calc(1.5rem + 4vw); }
```

### Meta viewport (obligatorio en páginas adaptativas)

```html
<meta name="viewport" content="width=device-width,initial-scale=1" />
```

---

## 15. Otras propiedades habituales

```css
opacity: 0.8;              /* Transparencia del elemento (0 a 1) */

box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* Sombra de caja */

cursor: pointer;           /* Cambia el cursor al pasar por encima */

transition: all 0.3s ease; /* Animación suave entre estados */

transform: scale(1.05);    /* Escalado */
transform: translateX(10px);
transform: rotateX(45deg);
```

---

## 16. Funciones CSS

```css
calc(1.5rem + 4vw)                     /* Expresión matemática */
rgb(255, 0, 0)                         /* Color */
rgba(255, 0, 0, 0.5)                   /* Color con transparencia */
url("imagen.jpg")                      /* Recurso externo */
repeat(3, 1fr)                         /* Repetición para grid */
minmax(100px, auto)                    /* Tamaño mín/máx para grid */
repeat(auto-fit, minmax(230px, 1fr))   /* Grid adaptativo */
```

---

## 17. Cascada, especificidad y herencia

### Orden de prioridad (de menor a mayor)

1. Declaraciones normales del navegador
2. Declaraciones normales del usuario
3. Declaraciones normales del autor
4. Declaraciones importantes del autor (`!important`)
5. Declaraciones importantes del usuario
6. Declaraciones importantes del navegador

### Especificidad (de menor a mayor)

| Tipo de selector | Especificidad |
|---|---|
| Universal `*` | `0,0,0,0` |
| Elemento (`p`, `h1`…) | `0,0,0,1` |
| Pseudo-elemento (`::first-line`) | `0,0,0,2` (suma con el elemento) |
| Clase, pseudo-clase, atributo | `0,0,1,0` |
| ID | `0,1,0,0` |
| Estilo en línea (`style="..."`) | `1,0,0,0` |

Si dos reglas tienen la misma especificidad, **gana la que aparece más tarde** en la hoja de estilos.

### Herencia

Las propiedades de texto (`color`, `font-family`, `font-size`…) **se heredan** de padres a hijos. Las de caja (`width`, `margin`, `padding`…) **no se heredan**.

Valores de control de herencia:

```css
color: inherit;  /* Toma el valor del padre */
color: initial;  /* Toma el valor inicial de la propiedad */
color: revert;   /* Vuelve al estilo por defecto del navegador */
color: unset;    /* Si se hereda → inherit; si no → initial */
```

### `!important`

```css
body { color: black !important; }
```

> Usar `!important` solo cuando no haya otra solución. Tiene la máxima prioridad dentro de su categoría.

---

## 18. Capas (`@layer`)

Se usan para gestionar la especificidad en proyectos con múltiples hojas de estilo.

```css
/* Capa con nombre */
@layer reset {
    button { padding: 30px; }
}

/* Capa anónima */
@layer {
    p { color: red; }
}

/* Capas anidadas */
@layer header {
    @layer navigation {
        .custom-link { text-decoration: none; }
    }
}

/* Capa importada */
@import url("archivo.css") layer(seccion-clientes);
```

> Las reglas **fuera de cualquier capa** tienen siempre mayor prioridad que las que están dentro de una capa, independientemente de la especificidad.

---

## 19. Modo de escritura

```css
writing-mode: horizontal-tb; /* Horizontal de arriba a abajo (por defecto) */
writing-mode: vertical-rl;   /* Vertical de derecha a izquierda */
writing-mode: vertical-lr;   /* Vertical de izquierda a derecha */
```

Propiedades lógicas (equivalentes a `width`/`height` independientes del modo de escritura):

```css
inline-size: 200px;  /* Equivale a width en modo horizontal */
block-size: 100px;   /* Equivale a height en modo horizontal */
```

---

## 20. Estilo de listas y enlaces

### Listas

```css
ul {
    list-style-type: disc;       /* Tipo de bullet: disc, circle, square */
    list-style-type: decimal;    /* Para listas ordenadas: decimal, upper-roman, lower-alpha… */
    list-style-type: none;
    list-style-position: inside; /* Posición del bullet: inside o outside */
    list-style-image: url("bullet.png"); /* Imagen como bullet */
    line-height: 1.6;
}
```

### Enlaces

```css
a:link    { text-decoration: none; color: #0000cc; }
a:visited { text-decoration: none; color: #ffcc33; }
a:hover   { text-decoration: underline; color: #999999; font-weight: bold; }
a:active  { text-decoration: none; color: #ff0000; }
```

---

## Referencia rápida de las 30 propiedades más usadas

| Propiedad | Ejemplo |
|---|---|
| `color` | `color: #000000` |
| `background-color` | `background-color: #f5f5f5` |
| `background-image` | `background-image: url("img.jpg")` |
| `background-size` | `background-size: cover` |
| `font-size` | `font-size: 16px` |
| `font-family` | `font-family: Arial, sans-serif` |
| `font-weight` | `font-weight: bold` |
| `font-style` | `font-style: italic` |
| `text-align` | `text-align: center` |
| `text-decoration` | `text-decoration: underline` |
| `text-transform` | `text-transform: uppercase` |
| `line-height` | `line-height: 1.5` |
| `margin` | `margin: 20px` |
| `padding` | `padding: 20px` |
| `border` | `border: 1px solid black` |
| `border-radius` | `border-radius: 8px` |
| `width` | `width: 300px` |
| `height` | `height: 200px` |
| `max-width` | `max-width: 100%` |
| `min-height` | `min-height: 100px` |
| `display` | `display: flex` |
| `justify-content` | `justify-content: center` |
| `align-items` | `align-items: center` |
| `gap` | `gap: 16px` |
| `position` | `position: relative` |
| `top` / `left` / `right` / `bottom` | `top: 10px` |
| `overflow` | `overflow: hidden` |
| `opacity` | `opacity: 0.8` |
| `box-shadow` | `box-shadow: 0 4px 10px rgba(0,0,0,0.2)` |
| `cursor` | `cursor: pointer` |
| `transition` | `transition: all 0.3s ease` |
| `transform` | `transform: scale(1.05)` |
| `z-index` | `z-index: 10` |
| `box-sizing` | `box-sizing: border-box` |