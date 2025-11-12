package com.ecci.taskmanager.data.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Clase encargada de realizar la conversión entre tipos de datos
 * que no son compatibles directamente con la base de datos Room.
 *
 * En este caso, convierte valores de tipo [Date] a [Long] y viceversa,
 * permitiendo que las fechas puedan almacenarse correctamente
 * en la base de datos en formato de marca de tiempo (timestamp).
 *
 * Esta clase se utiliza mediante anotaciones @TypeConverter,
 * que le indican a Room cómo manejar estos tipos personalizados.
 */
class DateConverter {

    /**
     * Convierte un valor de tipo [Long] (marca de tiempo en milisegundos)
     * a un objeto de tipo [Date].
     *
     * @param value Valor en milisegundos que representa una fecha.
     * Puede ser nulo si el campo correspondiente en la base de datos está vacío.
     * @return Un objeto [Date] correspondiente al valor recibido,
     * o `null` si el valor es nulo.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Convierte un objeto de tipo [Date] a un valor numérico [Long],
     * que representa la fecha en milisegundos desde el 1 de enero de 1970 (epoch).
     *
     * @param date Objeto [Date] a convertir. Puede ser nulo.
     * @return El tiempo en milisegundos correspondiente a la fecha,
     * o `null` si la fecha es nula.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
