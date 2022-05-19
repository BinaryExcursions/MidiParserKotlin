import java.util.concurrent.atomic.AtomicInteger

val filePath:String = "/Users/president/Desktop/FooSimple.mid"

fun main(args: Array<String>)
{
	Printer.printIsActive = false
	val midiRecord:MidiRecord = MidiRecord()
	val midirDataReader:MidiReader = MidiReader()
	var midiHeader:MidiRecordHeader? = null

	if(midirDataReader.openMidiFile(filePath) == false)
	{
		Printer.printMessage("Unable to open the file: ${filePath}")
		return
	}

	midiHeader = MidiRecordHeader()
	val lastIdxRead:Int = midirDataReader.readMidiRecordHeader(midiHeader) + 1
	midiRecord.header = midiHeader
	midiHeader = null //Just be certain we don't maintain two pointers - good practice

	val trackInfo:MidiTrack? = null

	val readIdx:AtomicInteger = AtomicInteger(lastIdxRead) //So we can essentially pass the int by reference.
	do {
		val trackInfo:MidiTrack? = midirDataReader.readTrack(readIdx)

		if(trackInfo != null)
			midiRecord.appendTrack(trackInfo)
	}while(trackInfo != null)
}

fun  testSomePrintValues()
{
	Printer.printByteValuesAsDecimal(0xB6u, 0xCAu, numBytes = 2)
	Printer.printByteValuesAsDecimal(0xB6u, 0xCAu, 0x5Cu, 0x23u)

	Printer.printByteValuesAsBinary(0xB6u, 0xCAu, 0x5Cu, 0x23u)
	Printer.printByteValuesAsBinary(0xB6u, 0xCAu, numBytes = 2)
}

//val age:AtomicInteger = AtomicInteger(13)
//foo(age)
//print(age)

fun foo(f:AtomicInteger)
{
	var X:Int = f.getAndIncrement()

	print("${X}\n")

	X = f.getAndIncrement()

	print("${X}\n")

	X = f.addAndGet(5)

	print("${X}\n")
}
