val MIDI_HDR_VALUE:UInt = 0x4D546864u //This is MThd
val MIDI_HDR_LEN_VALUE:UInt = 0x6u //This seems to be the standard number according to the specs

class MidiRecordHeader
{
	private var m_Title:UInt = 0u
	private var m_Length:UInt = 0u
	private var m_TimeDivision:UShort = 0u
	private var m_NumberOfTracks:UShort = 0u
	private var m_MidiFileType:MidiType = MidiType.SIMULTANEOUS

	var Title:UInt
		get() = m_Title
		set(value) {
			if(value != MIDI_HDR_VALUE)
				m_Title = 0xFFu
			else
				m_Title = value
		}

	var Length:UInt
		get() = m_Length
		set(value) {
			if(value != MIDI_HDR_LEN_VALUE)
				return

			m_Length = value
		}

	var TimeDivision:UShort
		get() = m_TimeDivision
		set(value){
			if(value == UShort.MIN_VALUE) return

			m_TimeDivision = value
		}

	var NumberOfTracks:UShort
		get() = m_NumberOfTracks
		set(value){
			if(value == UShort.MIN_VALUE) return

			m_NumberOfTracks = value
		}

	var MidiFileType:MidiType
		get() = m_MidiFileType
		set(value){m_MidiFileType = value}

	override fun toString() : String
	{
		print("The header info: ------------- ")
		Printer.printUInt32AsHex(m_Title, activePrintOverride = true)
		return ""
	}

	fun numberToMidiType(num:UShort) : MidiType
	{
		when(num.toUInt()) {
			0u ->
				return MidiType.SINGLE
			1u ->
				return MidiType.SIMULTANEOUS
			else ->
				return MidiType.SEQUENTIAL
		}
	}
}