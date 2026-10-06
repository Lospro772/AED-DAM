from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
REPORTS = ROOT / "target" / "surefire-reports"
TARGET = ROOT / "target"
SRC_MAIN = ROOT / "src" / "main" / "java" / "com" / "docencia"

BLOQUES = [
    "colecciones",
    "condicionales",
    "algoritmos",
    "cadenas",
    "numeros",
    "herencia",
    "fechas",
    "regex",
    "ficheros",
    "sqlite",
]

ALIASES = {
    "listservice": "colecciones",
    "setservice": "colecciones",
    "mapservice": "colecciones",
    "ifelseservice": "condicionales",
    "switchservice": "condicionales",
    "busquedaservice": "algoritmos",
    "ordenacionservice": "algoritmos",
    "stringservice": "cadenas",
    "integerservice": "numeros",
    "mathservice": "numeros",
    "herenciaservice": "herencia",
    "localdateservice": "fechas",
    "regexservice": "regex",
    "csvservice": "ficheros",
    "clientedbservice": "sqlite",
}

INTERFACES = {
    "colecciones": ["ListService.java", "SetService.java", "MapService.java"],
    "condicionales": ["IfElseService.java", "SwitchService.java"],
    "algoritmos": ["BusquedaService.java", "OrdenacionService.java"],
    "cadenas": ["StringService.java"],
    "numeros": ["IntegerService.java", "MathService.java"],
    "herencia": ["HerenciaService.java"],
    "fechas": ["LocalDateService.java"],
    "regex": ["RegexService.java"],
    "ficheros": ["CsvService.java"],
    "sqlite": ["ClienteDbService.java"],
}

PESO_TESTS = 8.0
PESO_DOCUMENTACION = 2.0


def detectar_bloque(nombre: str) -> str | None:
    nombre = nombre.lower()
    for bloque in BLOQUES:
        if bloque in nombre:
            return bloque
    for clave, bloque in ALIASES.items():
        if clave in nombre:
            return bloque
    return None


def normalizar_texto(texto: str) -> str:
    reemplazos = str.maketrans("áéíóúÁÉÍÓÚüÜñÑ", "aeiouAEIOUuUnN")
    texto = texto.translate(reemplazos).lower()
    texto = re.sub(r"\s+", " ", texto)
    return texto.strip()


def extraer_metodos_interface(interface_path: Path) -> list[dict]:
    if not interface_path.exists():
        return []

    texto = interface_path.read_text(encoding="utf-8")

    patron = re.compile(
        r"(?P<javadoc>/\*\*[\s\S]*?\*/)?\s*"
        r"(?:public\s+)?"
        r"(?P<retorno>[\w<>\[\], ?]+)\s+"
        r"(?P<metodo>\w+)\s*"
        r"\((?P<params>[^)]*)\)\s*;",
        flags=re.MULTILINE,
    )

    metodos = []

    for match in patron.finditer(texto):
        metodo = match.group("metodo")
        if metodo in {"if", "for", "while", "switch", "catch"}:
            continue

        retorno = match.group("retorno").strip()
        params_raw = match.group("params").strip()
        javadoc = match.group("javadoc") or ""

        parametros = []
        if params_raw:
            for param in params_raw.split(","):
                partes = param.strip().split()
                if partes:
                    parametros.append(partes[-1].replace("...", ""))

        metodos.append(
            {
                "metodo": metodo,
                "retorno": retorno,
                "parametros": parametros,
                "javadoc": javadoc,
            }
        )

    return metodos


def puntuar_javadoc_metodo(info: dict) -> tuple[float, list[str]]:
    """
    Puntuación por método:
    - 0.40 descripción suficiente.
    - 0.25 @param correcto para todos los parámetros.
    - 0.20 @return si el método no es void.
    - 0.15 @throws o mención de error/validación/excepción.
    """
    javadoc = info["javadoc"]
    texto = normalizar_texto(javadoc)
    puntos = 0.0
    errores = []

    if not javadoc:
        return 0.0, ["sin JavaDoc"]

    contenido = re.sub(r"^/\*\*|\*/$", "", javadoc.strip(), flags=re.MULTILINE)
    contenido_limpio = normalizar_texto(re.sub(r"[@*]", " ", contenido))

    if len(contenido_limpio) >= 25 and info["metodo"].lower() not in contenido_limpio[:10]:
        puntos += 0.40
    else:
        errores.append("descripcion insuficiente")

    parametros = info["parametros"]
    if parametros:
        params_documentados = re.findall(r"@param\s+(\w+)", javadoc)
        faltantes = [param for param in parametros if param not in params_documentados]
        if not faltantes:
            puntos += 0.25
        else:
            errores.append("faltan @param: " + ", ".join(faltantes))
    else:
        puntos += 0.25

    if info["retorno"] == "void":
        puntos += 0.20
    elif "@return" in javadoc:
        puntos += 0.20
    else:
        errores.append("falta @return")

    if "@throws" in javadoc or any(palabra in texto for palabra in ["excepcion", "error", "invalido", "validacion"]):
        puntos += 0.15
    else:
        errores.append("falta @throws o condicion de error")

    return round(puntos, 2), errores


def puntuar_documentacion_bloque(bloque: str) -> dict:
    interfaces = INTERFACES[bloque]
    total_metodos = 0
    suma_puntos = 0.0
    detalles = []

    for interface_name in interfaces:
        interface_path = SRC_MAIN / bloque / interface_name
        metodos = extraer_metodos_interface(interface_path)

        if not interface_path.exists():
            detalles.append(f"{interface_name}: no existe")
            continue

        if not metodos:
            detalles.append(f"{interface_name}: no se detectan métodos")
            continue

        for metodo in metodos:
            total_metodos += 1
            puntos, errores = puntuar_javadoc_metodo(metodo)
            suma_puntos += puntos

            if errores:
                detalles.append(
                    f"{interface_name}.{metodo['metodo']}: {puntos:.2f}/1.00 -> "
                    + "; ".join(errores)
                )

    if total_metodos == 0:
        return {
            "nota": 0.0,
            "total_metodos": 0,
            "detalles": detalles,
        }

    nota = round((suma_puntos / total_metodos) * 10, 2)

    return {
        "nota": nota,
        "total_metodos": total_metodos,
        "detalles": detalles,
    }


def calcular_resumen_tests() -> dict:
    resumen = {bloque: {"total": 0, "passed": 0, "failed": 0} for bloque in BLOQUES}

    for report in sorted(REPORTS.glob("TEST-*.xml")):
        root = ET.parse(report).getroot()
        nombre = root.attrib.get("name", report.name)
        bloque = detectar_bloque(nombre)
        if bloque is None:
            continue

        total = int(root.attrib.get("tests", 0))
        failed = (
            int(root.attrib.get("failures", 0))
            + int(root.attrib.get("errors", 0))
            + int(root.attrib.get("skipped", 0))
        )
        passed = max(0, total - failed)

        resumen[bloque]["total"] += total
        resumen[bloque]["passed"] += passed
        resumen[bloque]["failed"] += failed

    return resumen


def main() -> int:
    resumen_tests = calcular_resumen_tests()

    lineas = [
        "=== NOTA POR BLOQUES ===",
        "",
        "Cada bloque muestra:",
        f"- Nota tests sobre 10 y aportacion sobre {PESO_TESTS:.2f}",
        f"- Nota documentacion sobre 10 y aportacion sobre {PESO_DOCUMENTACION:.2f}",
        "- Nota del bloque sobre 10",
        "",
    ]

    for bloque in BLOQUES:
        total = resumen_tests[bloque]["total"]
        passed = resumen_tests[bloque]["passed"]
        failed = resumen_tests[bloque]["failed"]

        nota_tests = 0.0 if total == 0 else round((passed / total) * 10, 2)
        aportacion_tests = round((nota_tests / 10) * PESO_TESTS, 2)

        documentacion = puntuar_documentacion_bloque(bloque)
        nota_doc = documentacion["nota"]
        aportacion_doc = round((nota_doc / 10) * PESO_DOCUMENTACION, 2)

        nota_bloque = round(aportacion_tests + aportacion_doc, 2)

        lineas.append(f"=== {bloque.upper()} ===")
        lineas.append(f"Tests: {nota_tests:.2f}/10 ({passed}/{total} tests, {failed} fallos)")
        lineas.append(f"Aportacion tests: {aportacion_tests:.2f}/{PESO_TESTS:.2f}")
        lineas.append(f"Documentacion: {nota_doc:.2f}/10 ({documentacion['total_metodos']} metodos revisados)")
        lineas.append(f"Aportacion documentacion: {aportacion_doc:.2f}/{PESO_DOCUMENTACION:.2f}")
        lineas.append(f"Nota bloque: {nota_bloque:.2f}/10")
        if documentacion["detalles"]:
            lineas.append("Observaciones documentacion:")
            for detalle in documentacion["detalles"][:15]:
                lineas.append(f"- {detalle}")
            if len(documentacion["detalles"]) > 15:
                lineas.append(f"- ... {len(documentacion['detalles']) - 15} observaciones mas")
        else:
            lineas.append("Documentacion completa para este bloque")
        lineas.append("")

    informe = "\n".join(lineas) + "\n"
    TARGET.mkdir(parents=True, exist_ok=True)
    (TARGET / "notas_por_bloques.txt").write_text(informe, encoding="utf-8")
    print(informe, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
