import java.util.concurrent.atomic.AtomicInteger

/**
 * NOTE: When reading the MIDI spec for Meta-Events:
 * 2 lower case letters:  8-Bits
 * 4 lower case letters: 16-Bits
 * 6 lower case letters: 24-Bits
 */
class MetaEventParser
{
	private var m_Data:ByteArray? = null
	private var m_TimeDelta:UInt = 0u

	fun parseMetaEvent(startIdx:AtomicInteger, timeDelta:UInt, data:ByteArray) : IEvent?
	{
		m_Data = data
		m_TimeDelta = timeDelta

		var tmpIdx:Int = startIdx.get()
		val metaSimpleID:UShort = Utils.into16Bit(m_Data!![tmpIdx].toUByte(), m_Data!![tmpIdx + 1].toUByte())
		startIdx.addAndGet(2)

		var metaEventType:MetaEventDefinitions = MetaEventDefinitions.UNKNOWN

		for (evtID in enumValues<MetaEventDefinitions>())
		{
			if(evtID.num == metaSimpleID){
				metaEventType = evtID
				break
			}
		}

		if(metaEventType == MetaEventDefinitions.UNKNOWN)  {
			m_Data = null //Just for cleanliness. It's reset each time the function is called, but still good practice.
			return null
		}

		var event:IEvent? = parseAppropriateMetaEvent(startIdx, metaEventType)

		m_Data = null

		return event
	}

	private fun parseAppropriateMetaEvent(startIdx:AtomicInteger, eventType:MetaEventDefinitions) : IEvent?
	{
		var event:IEvent? = null

		when(eventType) {
			MetaEventDefinitions.TEXT_INFO, //0xFF01,
			MetaEventDefinitions.COPYRIGHT, //0xFF02 -
			MetaEventDefinitions.TEXT_SEQUENCE, //0xFF03 -
			MetaEventDefinitions.TEXT_INSTRUMENT, //0xFF04 -
			MetaEventDefinitions.TEXT_LYRIC, //0xFF05 -
			MetaEventDefinitions.TEXT_MARKER, //0xFF06 -
			MetaEventDefinitions.TEXT_CUE_POINT -> //0xFF07 -
				event = parseTextEvent(startIdx, eventType)
			MetaEventDefinitions.SEQUENCE_NUMBER ->
				event = parseSeqNumber(startIdx)
			MetaEventDefinitions.MIDI_CHANNEL ->
				event = parseMidiChannel(startIdx)
			MetaEventDefinitions.PORT_SELECTION ->
				event = parsePortSelection(startIdx)
			MetaEventDefinitions.TEMPO ->
				event = parseTempo(startIdx)
			MetaEventDefinitions.SMPTE ->
				event = parseSmpte(startIdx)
			MetaEventDefinitions.TIME_SIGNATURE ->
				event = parseTimeSignature(startIdx)
			MetaEventDefinitions.KEY_SIGNATURE ->
				event = parseKeySignature(startIdx)
			MetaEventDefinitions.SPECIAL_SEQUENCE ->
				event = parseSpecialSequence(startIdx)
			MetaEventDefinitions.END_OF_TRACK ->//0xFF2F
				{} //Already handled when we validated the track chunk
			else ->
				{}
		}

		return event
	}

	//0xFF00 - Will be followed by 02 then the sequence number
	private fun parseSeqNumber(startIdx:AtomicInteger) : IEvent?
	{
		val seqNum:UInt? = Utils.readVariableLengthValue(startIdx, m_Data!!)

		if(seqNum == null) return null

		//This particular event uses two bytes maximum for the sequence number
		return MetaEventSequenceNumber(metaEventType = MetaEventDefinitions.SEQUENCE_NUMBER, sequenceNumber = seqNum.toUShort())
	}

	//All 0xFF2[0 - F]

	//0xFF20 -
	private fun parseMidiChannel(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the Channel Prefix metaevent is 0xFF2001
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x01u) return null //Casting because Kotlin has serious issues handling unsigend variables across the board

		val midiChannel:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		Printer.printUInt8AsHex(midiChannel)

		return MetaEventChannel(channel = midiChannel, metaEventType = MetaEventDefinitions.MIDI_CHANNEL)
	}

	//0xFF21 - //Also has a 01 after the 21, and then a byte (0 - 127) identifying the port number.
	//This appears in some documents as optional and other as obsolete
	private fun parsePortSelection(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the port selection metaevent is 0xFF2101
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x01u) return null

		val portNumber:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		Printer.printUInt8AsHex(portNumber)

		return MetaEventOutputPort(PortNumber = portNumber,  metaEventType = MetaEventDefinitions.PORT_SELECTION)
	}

	//All 0xFF5[0 - F]
	//0xFF51 -
	private fun parseTempo(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the tempo metaevent is 0xFF5103
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x03u) return null

		//This will ultimately be used to store the 24 bit tempo
		var tempoValue:UInt

		var MSB:UInt = m_Data!![startIdx.get()].toUInt()   //UInt32(m_Data[startIdx])
		MSB = MSB shl 16  //(MSB << 16)
		startIdx.incrementAndGet()

		var MID_VAL:UInt =  m_Data!![startIdx.get()].toUInt()  //UInt32(m_Data[startIdx])
		MID_VAL = MID_VAL shl 8  //(MID_VAL << 8)
		startIdx.incrementAndGet()

		tempoValue = MSB + MID_VAL + m_Data!![startIdx.get()].toUInt()    //UInt32(m_Data[startIdx])
		startIdx.incrementAndGet()

		Printer.printUInt32AsHex(tempoValue)

		return MetaEventSetTempo(tempo = tempoValue, metaEventType = MetaEventDefinitions.TEMPO)
	}

	//0xFF54 -
	private fun parseSmpte(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the SMPTE metaevent is 0xFF5405
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x05u) return null

		//hr
		val hours:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//mn
		val minutes:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//se
		val seconds:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//fr
		val milliseconds:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//ff
		val fractionalFrames:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		Printer.printByteValuesAsHex(hours, minutes, seconds, milliseconds)
		Printer.printByteValuesAsHex(fractionalFrames)

		return MetaEventSMPTEOffset(hour = hours,
												minute = minutes,
												seconds = seconds,
												millisec = milliseconds,
												fractionalFrames = fractionalFrames,
												metaEventType = MetaEventDefinitions.SMPTE)
	}

	//0xFF58 -
	private fun parseTimeSignature(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the time signature metaevent is 0xFF5804
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x04u) return null

		val numerator:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//The denominator is a negative power of two. ie: 1^(-3) is really 1/3
		val denominator:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		var timing:TimingInfo = Utils.timeSignatureFromNumeratorDenominator(numerator, denominator)

		val midiClocksInMetronomeClick:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		val numberNotated32ndNotes:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		Printer.printByteValuesAsHex(numerator)
		Printer.printByteValuesAsHex(denominator)
		Printer.printByteValuesAsHex(midiClocksInMetronomeClick)
		Printer.printByteValuesAsHex(numberNotated32ndNotes)

		return MetaEventTimeSignature(timeSignature= timing,
												clockClicks = midiClocksInMetronomeClick,
												numberOf32ndNotes = numberNotated32ndNotes,
												metaEventType = MetaEventDefinitions.TIME_SIGNATURE)
	}

	//0xFF59 -
	private fun parseKeySignature(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the mini-time signature metaevent is 0xFF5902
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x02u) return null

		//We need this to be signed!
		//Negative represents the number of flats [-1 through -7]
		//Positive represents the number of sharps [1 through 7]
		//Zero reprsents C Major/A Minor
		val numberSharpsFlats:Byte = m_Data!![startIdx.get()]  // Int8(m_Data[startIdx]) //We WANT the sign bit
		startIdx.incrementAndGet()

		//0 = Major key
		//1 = Minor key
		val MajorMinorKey:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		val trackKey:MusicalKey = Utils.valuesToMusicalKey(numberSharpsFlats, MajorMinorKey)
		//Printer.printMessage(MusicalKey.musicalKeyToString(trackKey))

		return MetaEventKeySignature(keySignature = trackKey, metaEventType = MetaEventDefinitions.KEY_SIGNATURE)
	}

	/**
	 All meta-text events are 0xFF0[1 - F]
	 NOTE: In the text events, there is a size of the text + 1. It seems this is
	 a value of 0x00 for the string's null terminator. ie: '\0'
	 0xFF0[1 - F], //Followed by LEN, TEXT. NOTE: The 0xFF01 - 0xFF0F are all reserved for text messages.
	 Covers messages:
	 0xFF01
	 0xFF02
	 0xFF03
	 0xFF04
	 0xFF05
	 0xFF06
	 0xFF07
	*/
	private fun parseTextEvent(startIdx:AtomicInteger, eventType:MetaEventDefinitions) : IEvent?
	{
		var metaeventType:MetaEventDefinitions

		when(eventType) {
			MetaEventDefinitions.COPYRIGHT -> {metaeventType = MetaEventDefinitions.COPYRIGHT}
			MetaEventDefinitions.TEXT_INFO -> {metaeventType = MetaEventDefinitions.TEXT_INFO}
			MetaEventDefinitions.TEXT_LYRIC -> {metaeventType = MetaEventDefinitions.TEXT_LYRIC}
			MetaEventDefinitions.TEXT_MARKER -> {metaeventType = MetaEventDefinitions.TEXT_MARKER}
			MetaEventDefinitions.TEXT_SEQUENCE -> {metaeventType = MetaEventDefinitions.TEXT_SEQUENCE}
			MetaEventDefinitions.TEXT_CUE_POINT -> {metaeventType = MetaEventDefinitions.TEXT_CUE_POINT}
			MetaEventDefinitions.TEXT_INSTRUMENT -> {metaeventType = MetaEventDefinitions.TEXT_INSTRUMENT}
			else -> { return null}
		}

		val strLen:UInt? = Utils.readVariableLengthValue(startIdx, m_Data!!)

		if( (strLen == null) || (strLen == 0u) || (strLen.toInt() < 0) ) return null

		val s:String = Utils.bytesToString(strLen.toInt(), startIdx, m_Data!!)

		return MetaEventText(textInfo = s, metaEventType = metaeventType)
	}

	//Manufacturer specific events with 0xFF7[0 - F]

	//0xFF70 - 0xFF7F
	private fun parseSpecialSequence(startIdx:AtomicInteger) : IEvent?
	{
		//Even though this code does not implement manufacturer specific code, we still need to process
		//this message by reading off the appropriate bytes so that our buffer pointer does not get
		//out of sync and cause a corrupt read.

		val len:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		if(len > 0u)
			startIdx.addAndGet(len.toInt())

		return null
	}
}