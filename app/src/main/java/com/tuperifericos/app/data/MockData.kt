
package com.tuperifericos.app.data

import com.tuperifericos.app.model.CategoriaProducto
import com.tuperifericos.app.model.MotivoSalida
import com.tuperifericos.app.model.Movimiento
import com.tuperifericos.app.model.Perfil
import com.tuperifericos.app.model.Producto
import com.tuperifericos.app.model.TipoMovimiento
import com.tuperifericos.app.model.UnidadMedida
import com.tuperifericos.app.model.Usuario

object MockData {

    val usuarios = listOf(
        Usuario("U1", "Encargado de tienda (demo)", "encargado@tuperifericos.cl", "encargado123", Perfil.ENCARGADO),
        Usuario("U2", "Vendedor (demo)", "ventas@tuperifericos.cl", "ventas123", Perfil.USUARIO_AUTORIZADO)
    )

    val productos = listOf(
        Producto("P1", "TEC-001", "Teclado mecánico RGB", "Teclado mecánico switches rojos, layout español", CategoriaProducto.TECLADOS, UnidadMedida.UNIDAD, 22, 8, "Estante A1"),
        Producto("P2", "TEC-002", "Teclado inalámbrico compacto", "Teclado Bluetooth 75%", CategoriaProducto.TECLADOS, UnidadMedida.UNIDAD, 6, 5, "Estante A1"),
        Producto("P3", "MOU-001", "Mouse gamer 12000 DPI", "Mouse óptico con 6 botones programables", CategoriaProducto.MOUSE, UnidadMedida.UNIDAD, 25, 10, "Estante A2"),
        Producto("P4", "MOU-002", "Mouse inalámbrico ergonómico", "Mouse vertical recargable", CategoriaProducto.MOUSE, UnidadMedida.UNIDAD, 9, 6, "Estante A2"),
        Producto("P5", "AUD-001", "Audífonos gamer 7.1", "Audífonos con micrófono desmontable", CategoriaProducto.AUDIFONOS, UnidadMedida.UNIDAD, 14, 6, "Estante B1"),
        Producto("P6", "MON-001", "Monitor 24\" Full HD", "Monitor IPS 75 Hz HDMI", CategoriaProducto.MONITORES, UnidadMedida.UNIDAD, 5, 3, "Bodega C1"),
        Producto("P7", "WEB-001", "Cámara web 1080p", "Webcam con micrófono integrado", CategoriaProducto.WEBCAMS, UnidadMedida.UNIDAD, 12, 5, "Estante B2"),
        Producto("P8", "ACC-001", "Pack cables HDMI 1,5 m", "Pack de 5 cables HDMI 2.0", CategoriaProducto.ACCESORIOS, UnidadMedida.PACK, 30, 10, "Estante D1")
    )

    val movimientos = listOf(
        Movimiento("M1", "P1", "2026-10-01 09:15", TipoMovimiento.INGRESO, 20, responsable = "Encargado de tienda (demo)"),
        Movimiento("M2", "P1", "2026-10-02 16:40", TipoMovimiento.SALIDA, 2, MotivoSalida.VENTA, responsable = "Vendedor (demo)"),
        Movimiento("M3", "P3", "2026-10-03 11:05", TipoMovimiento.SALIDA, 5, MotivoSalida.VENTA, responsable = "Vendedor (demo)"),
        Movimiento("M4", "P6", "2026-10-04 10:30", TipoMovimiento.SALIDA, 1, MotivoSalida.DANADO, "Pantalla con rayas al desembalar", "Encargado de tienda (demo)"),
        Movimiento("M5", "P1", "2026-10-06 12:10", TipoMovimiento.SALIDA, 1, MotivoSalida.GARANTIA, "Cambio por tecla defectuosa", "Vendedor (demo)"),
        Movimiento("M6", "P1", "2026-10-08 15:20", TipoMovimiento.INGRESO, 5, observacion = "Reposición de proveedor ficticio", responsable = "Encargado de tienda (demo)")
    )
}
