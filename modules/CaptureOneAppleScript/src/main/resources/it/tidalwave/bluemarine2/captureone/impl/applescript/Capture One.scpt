global _output
global _path_separator
--global _application_name
global _excluded_collection_names

set _output to ""
set _path_separator to " :: "
--set _application_name to "Capture One 11"
set _excluded_collection_names to {"All Images", "Recent Imports", "Recent Captures", "Trash", "Filters", "In Catalog"}

tell application "Capture One 11"
    my dump_collections(current document)
    return _output
end tell

on dump_collections(_document)
    tell application "Capture One 11"
        set _collections to collections of _document
        my append("<?xml version='1.0' encoding='UTF-8'?>")
        my append("<document>")
        my append("  <name>" & name of _document & "</name>")
        my dump_collections_contents(collections of _document, "", "    ")
        my append("</document>")
    end tell
end dump_collections

on dump_collections_contents(_collections, _parent_name, _indent)
    tell application "Capture One 11"
        if (count of _collections) is not 0 then
            my append(_indent & "<collections>")
            repeat with _child_collection in _collections
                my dump_collection_contents(_child_collection, _parent_name, _indent & "  ")
            end repeat
            my append(_indent & "</collections>")
        end if
    end tell
end dump_collections_contents

on dump_collection_contents(_collection, _parent_name, _indent)
    tell application "Capture One 11"

        if _excluded_collection_names contains name of _collection then
            return
        end if

        if folder of _collection is equal to missing value then
            my append(_indent & "<!-- " & _parent_name & _path_separator & name of _collection & " -->")
            my append(_indent & "<collection>")
            my append(_indent & "  <name>" & name of _collection & "</name>")
            set _parent_name to _parent_name & _path_separator & name of _collection
            my dump_collections_contents(collections of _collection, _parent_name, _indent & "  ")

            set _images to images of _collection

            if (count of _images) is not 0 then
                my append(_indent & "  <images>")
                repeat with _image in _images
                    my append(_indent & "    <image>")
                    my append(_indent & "      <name>" & name of _image & "</name>")
                    my append(_indent & "      <path>" & path of _image & "</path>")
                    my append(_indent & "    </image>")
                end repeat
                my append(_indent & "  </images>")
            end if

            my append(_indent & "</collection>")
        end if
    end tell
end dump_collection_contents

on append(_string)
    log _string
    set _output to _output & _string & (ASCII character 13) & (ASCII character 10)
end append
