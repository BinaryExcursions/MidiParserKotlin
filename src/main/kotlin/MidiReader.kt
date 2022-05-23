import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class MidiReader
{
	private var m_ReadingIndex:Int = 0
	private var m_MidiData:ByteArray? = null

	//The event parsers
//	private lateinit var m_MetaParser:MetaEventParser
//	private lateinit var m_MidiParser:MidiEventParser

	private val m_MetaParser:MetaEventParser by lazy {MetaEventParser()}
	private val m_MidiParser:MidiEventParser by lazy { MidiEventParser() }

	fun openMidiFile(path:String) : Boolean
	{
		val filePath:String = path.takeIf { it.isNotEmpty() } ?: return false
		val fileHndl:File = File(filePath).takeIf { it.exists() && it.canRead() } ?: return false

		m_MidiData = fileHndl.readBytes()

		return (m_MidiData!!.size > 0)
	}

	//The return is the last index read. -1 is error
	fun readMidiRecordHeader(hdr:MidiRecordHeader) : Int
	{
		if(m_MidiData == null) return 0

		hdr.Title = Utils.into32Bit(m_MidiData!![0].toUByte(),
												m_MidiData!![1].toUByte(),
                                    m_MidiData!![2].toUByte(),
                                    m_MidiData!![3].toUByte())

		if(hdr.Title != MIDI_HDR_VALUE) return 3

		hdr.Length = Utils.into32Bit(m_MidiData!![4].toUByte(),
												m_MidiData!![5].toUByte(),
												m_MidiData!![6].toUByte(),
												m_MidiData!![7].toUByte())

		if (hdr.Length != MIDI_HDR_LEN_VALUE) return 7

		val hdrType:UShort = Utils.into16Bit(m_MidiData!![8].toUByte(), m_MidiData!![9].toUByte())

		if(hdrType > MIDI_TYPE_MAX) return 9

		hdr.MidiFileType = hdr.numberToMidiType(hdrType)

		hdr.NumberOfTracks = Utils.into16Bit(m_MidiData!![10].toUByte(), m_MidiData!![11].toUByte())

		if(hdr.NumberOfTracks <= 0u) return 11

		hdr.TimeDivision = Utils.into16Bit(m_MidiData!![12].toUByte(), m_MidiData!![13].toUByte())

		//Here so that any future updates to the MIDI standards means this error return value and the
		//value for a fully completed read of the MIDI header may be different - for now, yes - they're identical.
		if(hdr.TimeDivision <= 0u) return 13

		return 13
	}

	fun readTrack(startIndex:AtomicInteger) : MidiTrack?
	{
		if(m_MidiData == null) return null

		//The below constants are number of byte values
		val HDR_SIZE:Int = 4
		val EOT_SIZE:Int = 3 //3 bytes are used for the end of track marker
		val NUM_BYTES_TRACK_SIZE:Int = 4

		var idx:Int = startIndex.get()

		//Read track header
		val hdr:UInt = Utils.into32Bit(m_MidiData!![idx].toUByte(), m_MidiData!![idx + 1].toUByte(), m_MidiData!![idx + 2].toUByte(), m_MidiData!![idx + 3].toUByte())
		idx += HDR_SIZE

		if (hdr != MIDI_TRK_VALUE) return null

		val trkChunkSize:UInt = Utils.into32Bit(m_MidiData!![idx].toUByte(), m_MidiData!![idx + 1].toUByte(), m_MidiData!![idx + 2].toUByte(), m_MidiData!![idx + 3].toUByte())
		idx += NUM_BYTES_TRACK_SIZE

		val endIdx1:Int = startIndex.get() + HDR_SIZE + NUM_BYTES_TRACK_SIZE + trkChunkSize.toInt()  - EOT_SIZE

		if( (endIdx1 + 2) >= m_MidiData!!.size) return null //You've got a serious issue if you wind up here

		//Remember, we have NOT moved the read pointer, we did an index calculation.
		/***Therefore, you must remember to increment the reader by EOT_SIZE before returning.**/ //Not certain this comment is needed
		//We're doing this EOT check here to make certain we have a valid record before reading and parsing the whole thing only to find out later it may be bad.
		val eot:UInt = Utils.into32Bit(0x00u, m_MidiData!![endIdx1].toUByte(), m_MidiData!![endIdx1 + 1].toUByte(), m_MidiData!![endIdx1 + 2].toUByte())
		if(eot != END_OF_TRACK) return null //Again, you've got a serious issue if you wind up here

		val track:MidiTrack = MidiTrack()
		track.TrackBlockTitle = hdr

		// Be absolutely to do this here or your data stream will appear very corrupt
		startIndex.set(idx)

		do {
			val eventInfo:IEvent? = readTrackEvents(startIndex)

			track.appendEvent(eventInfo)
		}while(eventInfo != null)

		return track
	}

	//Remember, the first thing we should be reading per-event is the delta time then that's followed by the event
	private fun readTrackEvents(startIdx:AtomicInteger) : IEvent?
	{
		//Remember, the delta time is a variable number of bytes with the maximum being
		//4 bytes - but will most likely ever only be 2 bytes. This is that 7 byte thing...
		val trackDeltaTimeOffset: UInt = readDeltaOffsetTime(startIdx) ?: return null

		val event:IEvent? = parseEventData(startIdx,trackDeltaTimeOffset)

		if(event == null) Printer.printMessage("Failed to parse event")

		return event
	}

	/**
	 * You need to have already validated the class byte array is not null when you call this method
	 * Return: The return value from this method is the event's delta time.
	 */
	private fun readDeltaOffsetTime(startIdx:AtomicInteger) : UInt?
	{
		if( (startIdx.get() <0) || (startIdx.get() >= m_MidiData!!.size)) return null

		var readIdx:Int = startIdx.get()

		var deltaTime:UInt = 0u
		var deltaTimeByteValue:UByte = 0u
		var numberOfBytesRead:Int = 0 //We know we're going to read at least one byte

		do {
			deltaTimeByteValue = m_MidiData!![readIdx].toUByte()

			//You MUST clear out the leading bit before adding it to our deltatime value! Just simply
			//ALWAYS doing it since it does no harm regardless if the leading bit is 0 or 1
			//but keeps the logic and readablity clean
			val deltaTimeValueToAdd = deltaTimeByteValue and MSB_REST_VALUE //Need to use a tmp variable so the loop predicate still works correctly

			deltaTime = deltaTime shl (numberOfBytesRead * 8) //First shift our stored value. 8 because it is the size of a byte

			deltaTime += deltaTimeValueToAdd

			readIdx += 1
			numberOfBytesRead += 1
		}while ((deltaTimeByteValue and MSB_TEST_VALUE) == MSB_TEST_VALUE)

		startIdx.addAndGet(numberOfBytesRead)

		return deltaTime
	}

	//Remember, there are different types of events. So this high level call is here so we can
	//call the appropriate event parser based on type. The true work to get out all the event
	//data is done in one of the event parsing classes - not necessarily from here within the reader.
	private fun parseEventData(startIdx:AtomicInteger, timeDelta:UInt) : IEvent?
	{
		if( (startIdx.get() < 0) || (startIdx.get() >= m_MidiData!!.size) ) return null

		val eventByte1:UByte = m_MidiData!![startIdx.get()].toUByte()

		return if(eventByte1 == META_EVENT_IDENFIFIER) { //Is it a .META_EVENT
			m_MetaParser.parseMetaEvent(startIdx, timeDelta, m_MidiData!!) //You'll re-read the first byte of the meta record
		}
		//NOTE - This will include the system exclusive message as trying to process all the specific manufactures isn't realistic for a general MIDI parser.
		// users of this source will need to specifically develop the parsing and event structs specific to a MIDI device they want to handle the sysex event.
		else { //Is it a .MIDI_EVENT
			m_MidiParser.parseMidiEvent(startIdx, timeDelta, m_MidiData!!)
		}
	}
}