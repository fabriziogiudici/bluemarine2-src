/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://bitbucket.org/tidalwave/bluemarine2-src.git
 * %%
 * Copyright (C) 2015 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 *
 * *********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * *********************************************************************************************************************
 *
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.bluemarine2.model.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3FileReader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.bluemarine2.model.MediaItem.Metadata;
import it.tidalwave.bluemarine2.model.spi.MetadataSupport;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static it.tidalwave.bluemarine2.model.MediaItem.Metadata.*;
import static it.tidalwave.bluemarine2.util.Miscellaneous.*;
import org.jaudiotagger.tag.images.Artwork;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j @NoArgsConstructor(access = PRIVATE)
public final class AudioMetadataFactory
  {
    private final static List<FieldKey> MAPPED_TAGS = Arrays.asList(
        FieldKey.ARTIST, FieldKey.ALBUM, FieldKey.TITLE, FieldKey.TITLE, FieldKey.COMMENT,
        FieldKey.TRACK, FieldKey.DISC_NO, FieldKey.DISC_TOTAL, FieldKey.COMPOSER,
        FieldKey.MUSICBRAINZ_TRACK_ID, FieldKey.MUSICBRAINZ_WORK_ID, FieldKey.MUSICBRAINZ_DISC_ID, FieldKey.MUSICBRAINZ_ARTISTID);

    // FIXME: use interface and implementation
    @Nonnull
    public static Metadata loadFrom (final @Nonnull Path path)
      {
        Metadata metadata = new MetadataSupport(path);
        AudioFile audioFile = null;
        File file = null;

        try
          {
            final Path normalizedPath = normalizedPath(path.toAbsolutePath());
            log.debug("path: {}", normalizedPath);
            file = toFileBMT46(normalizedPath);
//            audioFile = AudioFileIO.read(aPath.toFile());
            audioFile = new MP3FileReader().read(file); // FIXME in some cases AudioFileIO doesn't get the right file extension
            final AudioHeader header = audioFile.getAudioHeader();
            final Tag tag = audioFile.getTag(); // FIXME: getFirst below... should get all?

            metadata = metadata.with(FILE_SIZE,       Files.size(normalizedPath))
                               .with(DURATION,        Duration.ofSeconds(header.getTrackLength()))
                               .with(BIT_RATE,        (int)header.getBitRateAsNumber())
                               .with(SAMPLE_RATE,     header.getSampleRateAsNumber())
                               .with(BITS_PER_SAMPLE, header.getBitsPerSample())
                               .with(CHANNELS,        parseOptionalInt(header.getChannels()))
                               .with(FORMAT,          Optional.ofNullable(header.getFormat()))
                               .with(ENCODING_TYPE,   Optional.ofNullable(header.getEncodingType()))

                               .with(ARTIST,          tag.getFirst(FieldKey.ARTIST))
                               .with(ALBUM,           tag.getFirst(FieldKey.ALBUM))
                               .with(TITLE,           tag.getFirst(FieldKey.TITLE))
                               .with(COMMENT,         tag.getAll(FieldKey.COMMENT))
                               .with(TRACK_NUMBER,    parseOptionalInt(tag.getFirst(FieldKey.TRACK)))
                               .with(DISK_NUMBER,     parseOptionalInt(tag.getFirst(FieldKey.DISC_NO)))
                               .with(DISK_COUNT,      parseOptionalInt(tag.getFirst(FieldKey.DISC_TOTAL)))
                               .with(COMPOSER,        tag.getFirst(FieldKey.COMPOSER))

                               .with(MBZ_TRACK_ID,    id(tag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID)))
                               .with(MBZ_WORK_ID,     id(tag.getFirst(FieldKey.MUSICBRAINZ_WORK_ID)))
                               .with(MBZ_DISC_ID,     id(tag.getFirst(FieldKey.MUSICBRAINZ_DISC_ID)))
                               .with(MBZ_ARTIST_ID,   optionalList(tag.getAll(FieldKey.MUSICBRAINZ_ARTISTID).stream()
                                                                                    .filter(s -> ((s != null) && !"".equals(s)))
                                                                                    .flatMap(s -> Stream.of(s.split("/"))) // FIXME:correct?
                                                                                    .map(s -> id(s))
                                                                                    .collect(toList())));

            for (final FieldKey fieldKey : FieldKey.values())
              {
                if (!MAPPED_TAGS.contains(fieldKey))
                  {
                    final Key<Object> key = new Key<Object>("tag." + fieldKey.name()) {};
                    final List<String> values = tag.getAll(fieldKey);

                    if (!values.isEmpty())
                      {
                        metadata = metadata.with(key, values);
                      }
                  }
              }

            metadata = metadata.with(ITUNES_COMMENT, ITunesComment.from(metadata));

            metadata = metadata.with(ARTWORK, tag.getArtworkList().stream().map(Artwork::getBinaryData).collect(toList()));

//            put(YEAR, Integer.valueOf(tag.getFirst(FieldKey.YEAR)));


//            tag.getFirst(FieldKey.ARTIST_SORT);

////            log.debug("Bitrate: " + mp3File.getBitrate()+ " kbps " + (mp3File.isVbr() ? "(VBR)" : "(CBR)"));
//
//            if (mp3File.hasId3v1Tag())
//              {
//                final ID3v1 id3v1Tag = mp3File.getId3v1Tag();
//                log.debug("Genre: " + id3v1Tag.getGenre() + " (" + id3v1Tag.getGenreDescription() + ")");
//              }
//
//            if (mp3File.hasId3v2Tag())
//              {
//                final ID3v2 id3v2Tag = mp3File.getId3v2Tag();
//                put(PUBLISHER, id3v2Tag.getPublisher());
//                log.debug("Original artist: " + id3v2Tag.getOriginalArtist());
//                log.debug("Album artist: " + id3v2Tag.getAlbumArtist());
//                log.debug("Copyright: " + id3v2Tag.getCopyright());
//                log.debug("URL: " + id3v2Tag.getUrl());
//                log.debug("Encoder: " + id3v2Tag.getEncoder());
//                final byte[] albumImageData = id3v2Tag.getAlbumImage();
//
//                if (albumImageData != null)
//                  {
//                    log.debug("Have album image data, length: " + albumImageData.length + " bytes");
//                    log.debug("Album image mime type: " + id3v2Tag.getAlbumImageMimeType());
//                  }
//              }

            log.trace(">>>> loaded keys for {}: {}", path, metadata.getKeys());
          }
        // FIXME: should we be more tolerant in general and expect an exception for every tag?
        // e.g. for wav files
        catch (UnsupportedOperationException e)
          {
            log.error("Unsupported tag in " + audioFile, e.toString());
          }
        catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e)
//        catch (IOException | CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e)
          {
            log.error("While reading " + audioFile + " --- " + path + " --- " + file, e);
          }

        return metadata;
      }

    @Nonnull
    private static <T> Optional<List<T>> optionalList (final @Nonnull List<T> list)
      {
        return list.isEmpty() ? Optional.empty() : Optional.of(list);
      }

    @Nonnull
    private static Optional<Integer> parseOptionalInt (final @Nullable String string)
      {
        try
          {
            return Optional.of(Integer.parseInt(string));
          }
        catch (NumberFormatException e)
          {
            return Optional.empty();
          }
      }

    @CheckForNull
    private static Id id (final @Nullable String string)
      {
        if ((string == null) || "".equals(string))
          {
            return null;
          }

        return new Id(string);
      }
  }
