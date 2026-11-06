/**
 * DoraAI Voice Module
 * Handles microphone access, audio recording, WAV encoding, and transcription.
 */

const AiVoice = {
    mediaRecorder: null,
    audioChunks: [],
    audioStream: null,
    isRecording: false,
    detectedLanguage: null,

    /**
     * Request microphone access and start recording
     */
    async startRecording() {
        try {
            this.audioStream = await navigator.mediaDevices.getUserMedia({ audio: true });
            this.audioChunks = [];

            // Use WAV format if available, otherwise fall back to default
            const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
                ? 'audio/webm;codecs=opus'
                : 'audio/webm';

            this.mediaRecorder = new MediaRecorder(this.audioStream, { mimeType });

            this.mediaRecorder.ondataavailable = (event) => {
                if (event.data.size > 0) {
                    this.audioChunks.push(event.data);
                }
            };

            this.mediaRecorder.onstop = async () => {
                const audioBlob = new Blob(this.audioChunks, { type: mimeType });
                await this.transcribeAudio(audioBlob);
            };

            this.mediaRecorder.start(100); // Collect data every 100ms
            this.isRecording = true;
            return true;
        } catch (error) {
            console.error('Microphone access denied:', error);
            this.isRecording = false;
            throw new Error('Microphone access denied. Please allow microphone permissions.');
        }
    },

    /**
     * Stop recording and process the audio
     */
    stopRecording() {
        if (this.mediaRecorder && this.isRecording) {
            this.mediaRecorder.stop();
            this.isRecording = false;

            // Stop all audio tracks
            if (this.audioStream) {
                this.audioStream.getTracks().forEach(track => track.stop());
                this.audioStream = null;
            }
        }
    },

    /**
     * Convert audio blob to base64 WAV format
     */
    async audioToWavBase64(audioBlob) {
        const arrayBuffer = await audioBlob.arrayBuffer();
        const audioBuffer = await this.decodeAudio(arrayBuffer);
        const wavBuffer = this.encodeWav(audioBuffer);
        return this.bufferToBase64(wavBuffer);
    },

    /**
     * Decode audio data using Web Audio API
     */
    async decodeAudio(arrayBuffer) {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const audioBuffer = await audioContext.decodeAudioData(arrayBuffer);

        // Convert to 16kHz mono PCM
        const offlineContext = new OfflineAudioContext(1, audioBuffer.length, 16000);
        const source = offlineContext.createBufferSource();
        source.buffer = audioBuffer;
        source.connect(offlineContext.destination);
        source.start(0);

        const renderedBuffer = await offlineContext.startRendering();
        audioContext.close();
        return renderedBuffer;
    },

    /**
     * Encode audio buffer to WAV format
     */
    encodeWav(audioBuffer) {
        const numChannels = 1; // Mono
        const sampleRate = 16000;
        const format = 1; // PCM
        const bitDepth = 16;

        const length = audioBuffer.length * numChannels * (bitDepth / 8);
        const buffer = new ArrayBuffer(44 + length);
        const view = new DataView(buffer);

        // WAV header
        this.writeString(view, 0, 'RIFF');
        view.setUint32(4, 36 + length, true);
        this.writeString(view, 8, 'WAVE');
        this.writeString(view, 12, 'fmt ');
        view.setUint32(16, 16, true);
        view.setUint16(20, format, true);
        view.setUint16(22, numChannels, true);
        view.setUint32(24, sampleRate, true);
        view.setUint32(28, sampleRate * numChannels * (bitDepth / 8), true);
        view.setUint16(32, numChannels * (bitDepth / 8), true);
        view.setUint16(34, bitDepth, true);
        this.writeString(view, 36, 'data');
        view.setUint32(40, length, true);

        // Write PCM samples
        const channelData = audioBuffer.getChannelData(0);
        let offset = 44;
        for (let i = 0; i < channelData.length; i++) {
            const sample = Math.max(-1, Math.min(1, channelData[i]));
            view.setInt16(offset, sample < 0 ? sample * 0x8000 : sample * 0x7FFF, true);
            offset += 2;
        }

        return buffer;
    },

    /**
     * Write a string to DataView
     */
    writeString(view, offset, string) {
        for (let i = 0; i < string.length; i++) {
            view.setUint8(offset + i, string.charCodeAt(i));
        }
    },

    /**
     * Convert ArrayBuffer to base64 string
     */
    bufferToBase64(buffer) {
        let binary = '';
        const bytes = new Uint8Array(buffer);
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary);
    },

    /**
     * Transcribe audio blob using the backend API
     */
    async transcribeAudio(audioBlob) {
        try {
            const wavBase64 = await this.audioToWavBase64(audioBlob);

            const response = await fetch('/api/ai/voice/transcribe', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    audioBase64: wavBase64,
                    language: this.detectedLanguage || undefined
                })
            });

            if (!response.ok) {
                throw new Error('Transcription failed');
            }

            const result = await response.json();
            this.detectedLanguage = result.language;

            // Callback to ai-chat.js
            if (window.onTranscriptionComplete) {
                window.onTranscriptionComplete(result.text, result.language);
            }

            return result;
        } catch (error) {
            console.error('Transcription error:', error);
            if (window.onTranscriptionError) {
                window.onTranscriptionError(error.message);
            }
            throw error;
        }
    },

    /**
     * Check if voice is supported in the current browser
     */
    isSupported() {
        return !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia);
    },

    /**
     * Get the current recording state
     */
    getRecordingState() {
        return this.isRecording;
    }
};
