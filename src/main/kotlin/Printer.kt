import jdk.jfr.Unsigned

class Printer
{
    companion object
    {
        var printIsActive:Boolean = true

        fun printMessage(msg:String, activePrintOverride:Boolean = false)
        {
            if(!printIsActive && !activePrintOverride) return

            print(msg)
        }

        fun printUInt8AsHex(X:UByte, activePrintOverride:Boolean = false)
        {
            if(!printIsActive && !activePrintOverride) return

            val s:String = Integer.toHexString(X.toInt()).uppercase()
            print("The UInt8 ${X} is: 0x${s}\n**********************\n")
        }

        fun printUInt32AsHex(X:UInt, activePrintOverride:Boolean = false)
        {
            if(!printIsActive && !activePrintOverride) return

            val s:String = Integer.toHexString(X.toInt()).uppercase()
            print("The UInt32 ${X} is: 0x${s}\n**********************\n")
        }

        fun printUInt16AsHex(X:UShort, activePrintOverride:Boolean = false)
        {
            if(!printIsActive && !activePrintOverride) return

            val s:String = Integer.toHexString(X.toInt()).uppercase()
            print("The UInt16 ${X} is: 0x${s})\n**********************\n")
        }

        fun printByteValuesAsHex(byte1:UByte, byte2:UByte = 0u, byte3:UByte = 0u, byte4:UByte = 0u, numBytes:Int = 4, activePrintOverride:Boolean = false)
        {
            if(!printIsActive && !activePrintOverride) return

            var msg:String = "Bytes Hex: "

            val b1:String = Integer.toHexString(byte1.toInt()).uppercase()
            msg += b1

            if(numBytes >= 2){
                val b2:String = Integer.toHexString(byte2.toInt()).uppercase()
                msg += " - ${b2}"
            }

            if(numBytes >= 3) {
                val b3:String = Integer.toHexString(byte3.toInt()).uppercase()
                msg += " - ${b3}"
            }

            if(numBytes >= 4) {
                val b4:String = Integer.toHexString(byte4.toInt()).uppercase()
                msg += " - ${b4}"
            }

            println(msg)
        }

        fun printByteValuesAsDecimal(byte1:UByte, byte2:UByte = 0u, byte3:UByte = 0u, byte4:UByte = 0u, numBytes:Int = 4, activePrintOverride:Boolean = false)
        {
            if(!Printer.printIsActive && !activePrintOverride) return

            val dec:UInt = Printer.bytesToUInt(byte1, byte2, byte3, byte4, numBytes)

            if(dec == UInt.MAX_VALUE) return

            val msg:String = "Bytes Dec: "

            println("${msg} 0x${Integer.toHexString(dec.toInt()).uppercase()}")
        }

        fun printByteValuesAsBinary(byte1:UByte, byte2:UByte = 0u, byte3:UByte = 0u, byte4:UByte = 0u, numBytes:Int = 4, activePrintOverride:Boolean = false)
        {
            if(!Printer.printIsActive && !activePrintOverride) return

            val dec:UInt = Printer.bytesToUInt(byte1, byte2, byte3, byte4, numBytes)

            if(dec == UInt.MAX_VALUE) return

            val msg:String = "Bytes Bin: "

            println("${msg} ${Integer.toBinaryString(dec.toInt())}")
        }

        private fun bytesToUInt(byte1:UByte, byte2:UByte = 0u, byte3:UByte = 0u, byte4:UByte = 0u, numBytes:Int = 4) : UInt
        {
            val MAX_NUM_BYTES:Int = 4
            val NUM_SHIFT_BITS:Int = 8

            if( (printIsActive == false) || (numBytes > MAX_NUM_BYTES) ) {
                return 0u
            }

            var dec:UInt = byte1.toUInt()
            var currByte:UByte

            for (idx in 0 until numBytes) {
                when(idx) {
                    0 -> continue //We actually have this case already taken care of currByte = byte1
                    1 -> currByte = byte2
                    2 -> currByte = byte3
                    3 -> currByte = byte4
                    else ->
                        break//Break from the loop
                }

                dec = dec shl NUM_SHIFT_BITS
                dec += currByte
            }

            return dec
        }
    }
}