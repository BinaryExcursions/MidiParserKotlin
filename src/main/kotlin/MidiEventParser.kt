import java.util.concurrent.atomic.AtomicInteger

import jdk.jfr.Unsigned
import kotlin.math.*

class MidiEventParser
{
	private var m_Data:ByteArray? = null
	private var m_TimeDelta:UInt = 0u

	fun parseMidiEvent(startIdx: AtomicInteger, timeDelta:UInt, data:ByteArray) : IEvent?
	{
		m_Data = data
		m_TimeDelta = timeDelta //Just don't want to pass this around to every method

		if(startIdx.get() >= m_Data!!.size) return null

		val midiStatusByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.addAndGet(1)

		val midiEvent:IEvent? = parseByteToMajorMidiMessage(midiStatusByte, startIdx)

		return midiEvent
	}

	private fun parseByteToMajorMidiMessage(messageValue:UByte, startIdx: AtomicInteger) : IEvent?
	{
		val SYS_COMMON_MSG_CTRl:UByte = 0xF0u
		val SYS_DATA_TYPE_CTRL:UByte = 0x08u

		var evt:IEvent?

		if( (messageValue and SYS_COMMON_MSG_CTRl) == SYS_COMMON_MSG_CTRl) { //Looking at the high 4 bits of the byte
			//Looking at the lower 4 bits of the byte but more specifically the first bit - if set = Sys Realtime if not sys common.
			//ie: If the last 4 bits are > 8 (ie: 0xF8 - 0xFD) its a sys realtime, but if less than 8 (ie: 0xF0 - 0xF7) It's a sys common message
			val dataInfoVal:UByte = messageValue and SYS_DATA_TYPE_CTRL

			if(dataInfoVal == SYS_DATA_TYPE_CTRL)
				evt = parseSystemRealTimeMessage(messageValue, startIdx)
			else
				evt = parseSystemExclusiveCommonMessage(messageValue, startIdx)
		}
		else {
			evt = parseChannelMessageType(messageValue, startIdx)
		}

		return evt
	}

	private fun parseChannelMessageType(messageValue:UByte, startIdx:AtomicInteger) : MidiChannelEvent?
	{
		val CHANNEL_CTRL:UByte = 0x0Fu
		val chnl:UByte = messageValue and CHANNEL_CTRL

		var byteRead:UByte = 0x0u

		if( (messageValue and MidiMajorMessage.NOTE_OFF.num) == messageValue) {
			byteRead = m_Data!![startIdx.get()].toUByte() //Reading the key note
			startIdx.incrementAndGet()

			val musicalNote =  MidiNote.convertByteToNote(byteRead)

			if(musicalNote == MidiNote.UNDEFINED) return null

			byteRead = m_Data!![startIdx.get()].toUByte() //Reading the Velocity
			startIdx.incrementAndGet()

			return null // MidiChannelEvent(eventTimeDelta:m_TimeDelta, channel:chnl, noteVelocity:byteRead, musicalNote:musicalNote, eventType:.NOTE_OFF)
		}
		else if( (messageValue and MidiMajorMessage.NOTE_ON.num) == messageValue) {
			byteRead = m_Data!![startIdx.get()].toUByte() //Reading the key note
			startIdx.incrementAndGet()

			val musicalNote =  MidiNote.convertByteToNote(byteRead)

			if(musicalNote == MidiNote.UNDEFINED) return null

			byteRead = m_Data!![startIdx.get()].toUByte() //Reading the Velocity
			startIdx.incrementAndGet()

			return null //MidiChannelEvent(eventTimeDelta:m_TimeDelta, channel:chnl, noteVelocity:byteRead, musicalNote:musicalNote, eventType:.NOTE_ON)
		}
		else if( (messageValue and MidiMajorMessage.KEY_PRESSURE_AFTER_TOUCH.num) == messageValue) {
			byteRead = m_Data!![startIdx.get()].toUByte() //Reading the key note
			startIdx.incrementAndGet()

			val musicalNote =  MidiNote.convertByteToNote(byteRead)

			if(musicalNote == MidiNote.UNDEFINED) return null

			byteRead = m_Data!![startIdx.get()].toUByte()//Reading the Pressure
			startIdx.incrementAndGet()

			return null // MidiChannelEvent(eventTimeDelta:m_TimeDelta, channel:chnl, pressure:byteRead, musicalNote:musicalNote, eventType:.KEY_PRESSURE_AFTER_TOUCH)
		}
		else if( (messageValue and MidiMajorMessage.CONTROL_CHANGE.num) == messageValue) {
			val byte1:UByte = m_Data!![startIdx.get()].toUByte() //Reading the controller number
			startIdx.incrementAndGet()

			byteRead = m_Data!![startIdx.get()].toUByte() //Reading the new control value
			startIdx.incrementAndGet()

			return null // MidiChannelEvent(eventTimeDelta:m_TimeDelta, channel:chnl, controllerNumber:byte1, controllerChangeValue:byteRead, eventType:.CONTROL_CHANGE)
		}
		else if( (messageValue and MidiMajorMessage.PROGRAM_CHANGE.num) == messageValue) {
			byteRead = m_Data!![startIdx.get()].toUByte() //Reading the new program number
			startIdx.incrementAndGet()

			return null // MidiChannelEvent(eventTimeDelta:m_TimeDelta, channel:chnl, programNumber:byteRead, eventType:.PROGRAM_CHANGE)
		}
		else if( (messageValue and MidiMajorMessage.CHANNEL_PRESSURE_AFTER_TOUCH.num) == messageValue) {
			byteRead = m_Data!![startIdx.get()].toUByte() //Reading the pressure value
			startIdx.incrementAndGet()

			return null //MidiChannelEvent(eventTimeDelta:m_TimeDelta, channel:chnl, pressure:byteRead, eventType:.CHANNEL_PRESSURE_AFTER_TOUCH)
		}
		else if( (messageValue and MidiMajorMessage.PITCH_WHEEL_CHANGE.num) == messageValue) {
			var pitch:UShort = 0x0000u

			var lsb:UByte = m_Data!![startIdx.get()].toUByte() //Reading the least significant bits value
			startIdx.incrementAndGet()

			var msb = m_Data!![startIdx.get()].toUByte() //Reading the most significant bits value
			startIdx.incrementAndGet()

			lsb = lsb and 0x7Fu //Be certain the leading bit is cleared or you could wind up with a full 16 bit value when you are only worried about 15 of them
			msb = msb and 0x7Fu //Be certain the leading bit is cleared or you could wind up with a full 16 bit value when you are only worried about 15 of them

			pitch = msb.toUShort()

			//Because Kotlin can't handle shifting anything but ints - HYPER ANNOYING!!!!!!!!
			var tmp:UInt = pitch.toUInt() shl 8
			pitch = tmp.toUShort() //We don't care about losing the first two bytes

			pitch = (pitch + lsb.toUShort()).toUShort()

			return null // MidiChannelEvent(eventTimeDelta:m_TimeDelta, channel:chnl, pitchWheelChange:pitch, eventType:.PITCH_WHEEL_CHANGE)
		}

		return null
	}

	private fun parseSystemExclusiveCommonMessage(messageValue:UByte, startIdx:AtomicInteger) : IEvent?
	{
		val SYS_MSG_IDENTIFIER_CTRL:UByte = 0x0Fu//So we can evaluate the lower 4 bits to identify the specific message

		var bytes:ArrayList<UByte>? = null
		var processMore:Boolean = true

		var msgIdToProcess:UByte = messageValue

		do {
			//There's a high probable that you don't want to add the start and stop bytes to your byte array, but you may.  If you do
			//then you'll want to update the 0th and 7th case.
			when((msgIdToProcess and SYS_MSG_IDENTIFIER_CTRL).toInt()) {
				0 ->//.SYS_EXCLUSIVE - Start
					bytes = ArrayList<UByte>() //Only once we know for certain we have the "start" of the exclusive message to we allocate our byte array

				//--NOTE: To the end user, you may want to do more with the system exclusive message so I left in the commented out cases
				//so you can see where you may want to provide more implementation specific to a particular manufacture's MIDI implementation
				2 -> {}//.SONG_POSITION_POINTER
				3 -> {}//.SONG_SELECT
				6 -> {}//.TUNE_REQUEST
				7 ->//.END_OF_EXCLUSIVE --End message
					{
						processMore = false
						startIdx.decrementAndGet()//We read the end of message - if we don't reset the counter here, the increment after the switch will get us our of sync
					}
				1, 4, 5 ->//.UNDEFINED
					{}  //As per the spec - these values are undefined System Common message types
				else ->
					if(bytes != null){bytes.add(msgIdToProcess)}
			}

			startIdx.incrementAndGet()
			msgIdToProcess = m_Data!![startIdx.get()].toUByte()
		} while(processMore == true)

		return null//SysExclusionEvent(eventTimeDelta: m_TimeDelta, exclusiveInfo: bytes ?? [])
	}

	private fun parseSystemRealTimeMessage(messageValue:UByte, idx:AtomicInteger) : IEvent?
	{
		val SYS_MSG_IDENTIFIER_CTRL:UByte = 0x07u//So we can evaluate the lower 3 bits to identify the specific message
		var messageInfo:Pair<MidiMajorMessage, UByte?> = Pair<MidiMajorMessage, UByte?>(MidiMajorMessage.UNDEFINED, null)
		//var messageInfo:Pair<>(msg:MidiMajorMessage, channel:UInt8?) = (.UNDEFINED, nil)

		var midiMessage:MidiMajorMessage = MidiMajorMessage.UNDEFINED

		when((messageValue and SYS_MSG_IDENTIFIER_CTRL).toInt()) {
			0 ->
				midiMessage = MidiMajorMessage.TIMING_CLOCK
			2 ->
				midiMessage = MidiMajorMessage.START_SEQUENCE
			3 ->
				midiMessage = MidiMajorMessage.CONTINUE_AT_POINT_OF_SEQUENCE_STOP
			4 ->
				midiMessage = MidiMajorMessage.STOP_SEQUENCE
			6 ->
				midiMessage = MidiMajorMessage.ACTIVE_SENSING
			7 ->
				midiMessage = MidiMajorMessage.RESET
			1, 5 ->
				midiMessage = MidiMajorMessage.UNDEFINED //As per the spec - these values are undefined System Common message tyeps
			else ->
				midiMessage = MidiMajorMessage.UNDEFINED
		}

		if(midiMessage == MidiMajorMessage.UNDEFINED) {
			Printer.printMessage("The System Real-time event was read as undefined.")
		}

		return null//SysRealtimeEvent(eventTimeDelta: m_TimeDelta)
	}
}