#!/usr/bin/ruby -w
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# $Id$

$KCODE = 'UTF8'

require 'logger'
require 'jcode'

# Taken from the AFM metrics file of the Nimbus Roman No9 L font, which has metrics
# compatible with the Times font that will be used by dot to produce graphics
TimesCharWidths = {
    ' ' => 250,
    '!' => 333,
    '#' => 500,
    '$' => 500,
    '%' => 833,
    '&' => 778,
    '”' => 333,
    '(' => 333,
    ')' => 333,
    '*' => 500,
    '+' => 564,
    ',' => 250,
    '-' => 333,
    '.' => 250,
    '/' => 278,
    '0' => 500,
    '1' => 500,
    '2' => 500,
    '3' => 500,
    '4' => 500,
    '5' => 500,
    '6' => 500,
    '7' => 500,
    '8' => 500,
    '9' => 500,
    ':' => 278,
    ';' => 278,
    '<' => 564,
    '=' => 564,
    '>' => 564,
    '?' => 444,
    '@' => 921,
    'A' => 722,
    'B' => 667,
    'C' => 667,
    'D' => 722,
    'E' => 611,
    'F' => 556,
    'G' => 722,
    'H' => 722,
    'I' => 333,
    'J' => 389,
    'K' => 722,
    'L' => 611,
    'M' => 889,
    'N' => 722,
    'O' => 722,
    'P' => 556,
    'Q' => 722,
    'R' => 667,
    'S' => 556,
    'T' => 611,
    'U' => 722,
    'V' => 722,
    'W' => 944,
    'X' => 722,
    'Y' => 722,
    'Z' => 611,
    '_' => 500,
    '“' => 333,
    'a' => 444,
    'b' => 500,
    'c' => 444,
    'd' => 500,
    'e' => 444,
    'f' => 333,
    'g' => 500,
    'h' => 500,
    'i' => 278,
    'j' => 278,
    'k' => 500,
    'l' => 278,
    'm' => 778,
    'n' => 500,
    'o' => 500,
    'p' => 500,
    'q' => 500,
    'r' => 333,
    's' => 389,
    't' => 278,
    'u' => 500,
    'v' => 500,
    'w' => 722,
    'x' => 500,
    'y' => 500,
    'z' => 444}

def assert(test)
    raise "AssertionFailedError" unless test
end

$log = Logger.new(STDERR)
$log.level = Logger::DEBUG
$log.formatter = Logger::Formatter.new
class <<$log.formatter
    def call(severity, time, progname, msg)
        msg + "\n"
    end
end

class KnuthElement
    attr_reader :w

    def initialize(w = 0, content = "")
        @w = w
        @content = content
    end

    def to_s
        if @content
            " \"#{@content}\""
        else
            ""
        end
    end

    def getLabel
        @content
    end
end

class Box < KnuthElement
    def initialize(w, content)
        super(w, content)
    end

    def to_s
        return "Box  w = #{@w}" + super
    end
end

class Glue < KnuthElement
    attr_reader :stretch, :shrink
    def initialize(w, stretch = 0, shrink = 0, content = " ")
        super(w, content)
        @stretch = stretch
        @shrink = shrink
    end

    def to_s
        return "Glue w = #{@w} str = #{@stretch} shr = #{@shrink}" + super
    end
end

class Penalty < KnuthElement
    attr_reader :p

    Infinite = 1000

    def initialize(w, p = 0, content = nil)
        super(w, content)
        @p = p
    end

    def is_forced_break?
        @p <= -Infinite
    end

    def to_s
        return "Pen  w = #{@w} p = #{@p}" + super
    end
end

# A box representing a line of a paragraph
class LineBox < KnuthElement
    def initialize(elements, difference)
        @elements = elements
        @difference = difference
    end

    def getLabel
        label = ""
        if @difference > 0
            nb_glues = (@elements.select { |e| e.instance_of? Glue }).length
            if nb_glues > 1
                nb_spaces = @difference / TimesCharWidths[' ']
                q, r = nb_spaces.divmod(nb_glues)
                spaces = []
                r.times { spaces.push(q + 1) }
                (nb_glues - r).times { spaces.push(q) }
                @elements.each do |elem|
                    if elem.instance_of? Glue
                        label << elem.getLabel + ' ' * spaces.pop
                    else
                        label << elem.getLabel
                    end
                end
            else
                @elements.each do |elem|
                    label << elem.getLabel
                end
            end
        else
            @elements.each do |elem|
                label << elem.getLabel
            end
        end
        label
    end

    def to_s
        label = ""
        @elements.each do |elem|
            label << elem.getLabel
        end
        label << '\n'
    end
end

class Paragraph

    def initialize(content)
        @content = content
    end

    def get_knuth_elements
        elements = []
        @content.each do |word|
            elements << Box.new(width(word), word)
            elements << Penalty.new(0, 0)
            elements << Glue.new(TimesCharWidths[' '], TimesCharWidths[' '] / 2,
                                TimesCharWidths[' '] / 3)
        end
        elements[-1] = Glue.new(0, 1000000, 0, "")
        elements << Penalty.new(0, -Penalty::Infinite)
    end

    private

    def width(word)
        w = 0
        for i in 0...word.size
            w += TimesCharWidths[word[i..i]]
        end
        w
    end
end

class ProgressInfo
    attr_accessor :total_length, :total_stretch, :total_shrink, :part

    def initialize(*infos)
        case infos.length
        when 0
            # Default initialization
            @total_length = @total_stretch = @total_shrink = @part = 0
        when 1
            # Argument is a ProgressInfo object
            @total_length  = infos[0].total_length
            @total_stretch = infos[0].total_stretch
            @total_shrink  = infos[0].total_shrink
            @part          = infos[0].part
        end
    end

    def reset
        @total_length = @total_stretch = @total_shrink = @part = 0
    end

    def eql?(other)
        @total_length == other.total_length and
                @total_stretch == other.total_stretch and
                @total_shrink == other.total_shrink and
                @part == other.part
    end

    def hash
        @total_length.hash
    end

    def to_s
        "#@total_length+#@total_stretch−#@total_shrink"
    end
end

class Layout
    attr_accessor :total_demerits, :previous, :progress, :alternatives, :elements

    def initialize(*other)
        case other.length
        when 0
            @total_demerits = 0
            @previous       = nil
            @progress       = ProgressInfo.new
            @alternatives   = []
            @elements       = []
        when 1
            @total_demerits = other[0].total_demerits
            @previous       = other[0].previous
            @progress       = ProgressInfo.new(other[0].progress)
            @alternatives   = other[0].alternatives.dup
            @elements       = other[0].elements.dup
        end
    end

    def dup
        new_layout = Layout.new
        new_layout.total_demerits = @total_demerits
        new_layout.previous       = @previous
        new_layout.progress       = ProgressInfo.new(@progress)
        new_layout.alternatives   = @alternatives
        new_layout.elements       = @elements.dup
        return new_layout
    end

    def get_block_progress
        @progress
    end

    # Dumps this layout to stdout in dot format, if not present in the handled list
    def to_dot(handled)
        unless handled.include?(object_id)
            handled << object_id
            if previous.nil?
                puts %(  "#{object_id}" [label="" shape=box width=0.2 height=0.2])
            else
                previous_layout = previous
                #if instance_of? LineLayout and is_page
                #    previous_layout = previous_layout.previous
                #end
                if previous_layout.instance_of? LineLayout and
                        previous_layout.line_layout.progress.part == 0
                    previous_layout = previous_layout.previous
                end
                previous_layout.to_dot(handled)
                label = ""
                shape = ""
                if instance_of? LineLayout and is_page or progress.total_length == 0
                    shape = "shape=box"
                    previous.elements.each do |elem|
                        label << elem.getLabel + '\l'
                    end
                else
                    elements.each do |elem|
                        label << elem.getLabel + '\l'
                    end
                end
                puts %(  "#{object_id}" [#{shape} label="#{label}"])
                puts %(  "#{previous_layout.object_id}" -> "#{object_id}")
                alternatives.each do |alt|
                    alt.to_dot(handled)
                    puts %(  "#{alt.object_id}" -> "#{object_id}" [style=dashed])
                end
            end
        end
    end

    def to_s
        label = "[ "
        elements.each { |e| label << "#{e} " }
        label << "]"
        label
    end
end

class LineLayout < Layout
    attr_accessor :line_layout, :is_page

    def initialize(*block_layout)
        super(*block_layout)
        @line_layout = Layout.new
        @is_page = false
        case block_layout.length
        when 0 # nop
        when 1
            @previous = block_layout[0]
        end
    end

    def dup
        new_layout = LineLayout.new(self)
        new_layout.previous    = @previous
        new_layout.line_layout = @line_layout.dup
        new_layout.is_page     = @is_page
        return new_layout
    end

    def getLineLabel
        label = "[ "
        line_layout.elements.each { |e| label << e.to_s }
        label << " ]"
        label
    end
end

class FeasibleBreaks

    def initialize
        @best = nil
        @alternatives = []
    end

    def add(layout, demerits, difference=0)
        if @best.nil?
            @best = {'dem' => demerits, 'layout' => layout, 'diff' => difference}
        elsif demerits < @best['dem']
            @alternatives << @best['layout']
            @best = {'dem' => demerits, 'layout' => layout, 'diff' => difference}
        else
            @alternatives << layout
        end
    end

    def get_best
        @best
    end

    def get_alternatives
        @alternatives
    end
end

class ActiveLayouts
    def initialize
        @layouts = []
    end

    def add(layout)
        progress = layout.get_block_progress
        if @layouts[progress.part]
            if @layouts[progress.part][progress]
                @layouts[progress.part][progress] << layout
            else
                @layouts[progress.part][progress] = [layout]
            end
        else
            @layouts[progress.part] = {progress => [layout]}
        end
    end

    def delete(layout)
        progress = layout.get_block_progress
        layouts = @layouts[progress.part][progress].delete(layout)
    end

    def clear
        @layouts = []
    end

    def empty?
        @layouts.inject(true) do |empty, hash|
            empty and (hash.nil? or empty_hash?(hash))
        end
    end

    def each(&block)
        each_block_class do |block_layouts|
            each_block_layout(block_layouts, &block)
        end
    end

    def each_block_class
        @layouts.each do |block_layouts|
            yield block_layouts if block_layouts && !empty_hash?(block_layouts)
        end
    end

    def each_line_class(&block)
        each_block_class do |block_layouts|
            block_layouts.values.each(&block)
        end
    end

    def each_block_layout(block_layouts, &block)
        block_layouts.each_value do |layouts|
            layouts.collect.each(&block)
        end
    end

    def each_line_layout(line_layouts, &block)
        line_layouts.collect.each(&block)
    end

    # Dumps the layouts on the standard output in the dot format
    def to_dot

        handled = []
        # page="8.27,11.69"
        # size="11,16"
        # margin="0.3"
        # node [shape=none fontname="NimbusRomNo9L-Regu"]
        puts %(digraph ActiveNodes  {
nodesep=.5; ranksep=1.5
node [shape=none]
edge [dir=none])
        each do |layout|
            layout.to_dot(handled)
        end
        puts "}"
    end

    def to_s
        str = ""
        pages = []
        for i in 0...@layouts.size
            pages << i if @layouts[i] && !empty_hash?(@layouts[i])
        end
        if pages.empty?
            "[empty]\n"
        else
            pages.sort!
            content_to_s(str, "", "", pages) do |str, prefix_first, prefix_rest, page|
                keys = []
                block_layouts = @layouts[page]
                block_layouts.each_key do |progress|
                    if block_layouts[progress] and !block_layouts[progress].empty?
                        keys << progress 
                    end
                end
                keys.sort! { |a, b| a.total_length <=> b.total_length }
                content_to_s(str, prefix_first, prefix_rest, keys) do
                        |str, prefix_first, prefix_rest, key|
                    end_content_to_s(str, prefix_first, prefix_rest, block_layouts[key])
                end
            end
            str
        end
    end

    private

    def end_content_to_s(str, prefix_first, prefix_rest, content)
        if content.length == 1
            str << "#{prefix_first}─── #{content[0]}\n"
        else
            str << "#{prefix_first}─┬─ #{content[0]}\n"
            content[1..-2].each do |c|
                str << "#{prefix_rest} ├─ #{c}\n"
            end
            str << "#{prefix_rest} └─ #{content[-1]}\n"
        end
    end

    def content_to_s(str, prefix_first, prefix_rest, content)
        if content.length == 1
            label = content[0].to_s
            first = prefix_first + "─── " + label + " "
            rest = prefix_rest + " " * (4 + label.jlength + 1)
            yield(str, first, rest, content[0])
        else
            label = content[0].to_s
            first = prefix_first + "─┬─ " + label + " "
            rest = prefix_rest + " │  " + " " * (label.jlength + 1)
            yield(str, first, rest, content[0])
            content[1..-2].each do |c|
                label = c.to_s
                first = prefix_rest + " ├─ " + label + " "
                rest = prefix_rest + " │  " + " " * (label.jlength + 1)
                yield(str, first, rest, c)
            end
            label = content[-1].to_s
            first = prefix_rest + " └─ " + label + " "
            rest = prefix_rest + " " * (4 + label.jlength + 1)
            yield(str, first, rest, content[-1])
        end
    end

    def empty_hash?(hash)
        hash.values.inject(true) do |empty, a|
            empty && a.empty?
        end
    end
end

class Breaker

    private_class_method :new

    def find_breaks(content)
        init_breaking
        content.each do |element|
            if element.instance_of? Box
                new_layouts = []
                @layouts.each do |layout|
                    new_layouts << layout
                    get_progress(layout).total_length += element.w
                    get_elements(layout) << element
                end
                @layouts.clear
                new_layouts.each { |l| @layouts.add(l) }
            elsif element.instance_of? Glue
                new_layouts = []
                @layouts.each do |layout|
                    new_layouts << layout
                    elements = get_elements(layout)
                    unless elements.empty?
                        progress = get_progress(layout)
                        progress.total_length += element.w
                        progress.total_stretch += element.stretch
                        progress.total_shrink += element.shrink
                        elements << element
                    end
                end
                @layouts.clear
                new_layouts.each { |l| @layouts.add(l) }
            elsif element.instance_of? Penalty
                consider_break(element) if element.p < Penalty::Infinite
            else
                handle_element(element)
            end
        end
        the_end
    end

    private

    def init_breaking
    end

    def get_progress(layout)
    end

    def get_elements(layout)
    end

    def consider_break(element)
    end

    def the_end
    end
end

class PageBreaker < Breaker

    public_class_method :new

    def initialize(page_break_handler, line_breaker)
        @page_break_handler = page_break_handler
        @line_breaker = line_breaker
    end

    def init_breaking
        @layouts = ActiveLayouts.new
        @layouts.add(Layout.new)
    end

    def get_progress(layout)
        layout.progress
    end

    def get_elements(layout)
        layout.elements
    end

    def consider_break(element)
        @page_break_handler.consider_break(element, @layouts)
    end

    def handle_element(element)
        if element.instance_of? Paragraph
            line_layouts = []
            @layouts.each do |layout|
                line_layouts << LineLayout.new(layout)
            end
            @layouts.clear
            line_layouts.each { |l| @layouts.add(l) }
            content = element.get_knuth_elements
            @line_breaker.layouts = @layouts
            @line_breaker.find_breaks(content)
        end
    end

    # Dumps layouts to dot format
    def the_end
        @layouts.to_dot
    end
end

class LineBreaker < Breaker

    public_class_method :new

    attr_writer :layouts

    def initialize(line_break_handler)
        @line_break_handler = line_break_handler
    end

    def get_progress(layout)
        layout.line_layout.progress
    end

    def get_elements(layout)
        layout.line_layout.elements
    end

    def consider_break(element)
        @line_break_handler.consider_break(element, @layouts)
    end
end

class LegalBreakHandler

    private_class_method :new

    InfiniteRatio = 1000

    def initialize(page_dimensions)
        @page_dims = page_dimensions
    end

    def consider_break(element, layouts)
        new_layouts = ActiveLayouts.new
        each_class(layouts) do |layout_class|
            feasible_breaks = FeasibleBreaks.new
            each_layout(layouts, layout_class) do |layout|
                #$log.debug("Considering #{layout}")
                difference = compute_difference(layout)
                difference -= element.w if element.instance_of? Penalty
                ratio = compute_adjustment_ratio(get_progress(layout), difference)
                #$log.debug("Ratio: #{ratio}")
                if ratio < -1.0 or element.is_forced_break?
                    difference = 0 # TODO
                    #$log.debug("Removing line-level layout: #{layout}")
                    layouts.delete(layout)
                end
                if -1.0 <= ratio and ratio <= get_threshold
                    d = compute_demerits(element, ratio) + get_demerits(layout)
                    feasible_breaks.add(layout, d, difference)
                end
            end
            best = feasible_breaks.get_best
            unless best.nil?
                new_layouts.add(create_layout(best, feasible_breaks.get_alternatives))
            end
        end
        unless new_layouts.empty?
            handle_new_layouts(new_layouts)
            new_layouts.each { |layout| layouts.add(layout) }
            $log.debug("After break:")
            $log.debug("#{layouts}")
        end
    end

    private

    # Executes the given block for each layout class
    def each_class(layouts, &block)
    end

    # Executes the given block for each layout of the given class
    def each_layout(layouts, layout_class, &block)
    end

    # Returns the maximum adjustment ratio allowed for feasible breaks
    def get_threshold
    end

    # Computes the difference between the available space and the space occupied by
    # the given layout
    def compute_difference(layout)
    end

    # Returns the progress informations for the given layout, that will be used to
    # compute the adjustment ratio
    def get_progress(layout)
    end

    # Returns the demerits of the given layout
    def get_demerits(layout)
    end

    # Creates and returns a new layout based on the given best break
    def create_layouts(best, alternatives)
    end

    # Performs necessary stuff after layouts for the current legal break have been
    # created. Basically, if inside a paragraph (line-level), call the page-level
    # breaker
    def handle_new_layouts(new_layouts)
    end

    def compute_adjustment_ratio(progress, difference)
        if difference > 0.0
            stretch = progress.total_stretch
            #$log.debug("Stretch: #{stretch}")
            if stretch > 0
                return difference.to_f / stretch
            else
                return InfiniteRatio
            end
        elsif difference < 0.0
            shrink = progress.total_shrink
            #$log.debug("Shrink: #{shrink}")
            if shrink > 0
                return difference.to_f / shrink
            else
                return -InfiniteRatio
            end
        else
            return 0
        end
    end

    def compute_demerits(element, ratio)
        d = 1 + 100 * ratio.abs**3
        if element.instance_of?(Penalty) && element.p > 0
            d = (d + element.p)**2
        elsif element.instance_of?(Penalty) && !element.is_forced_break?
            d = d**2 - element.p**2
        else
            d **= 2
        end
        return d
    end

end

class LegalLineBreakHandler < LegalBreakHandler

    public_class_method :new

    def initialize(page_dimensions, paragraph_break_handler)
        super(page_dimensions)
        @paragraph_break_handler = paragraph_break_handler
    end

    def each_class(layouts, &block)
        layouts.each_line_class(&block)
    end

    def each_layout(layouts, layout_class, &block)
        layouts.each_line_layout(layout_class, &block)
    end

    def get_threshold
        7.6
    end

    def compute_difference(layout)
        @page_dims[layout.progress.part][:ipd] -
                layout.line_layout.progress.total_length
    end

    def get_progress(layout)
        layout.line_layout.progress
    end

    def get_demerits(layout)
        layout.line_layout.total_demerits
    end

    def create_layout(best, alternatives)
        new_layout = LineLayout.new(best['layout'])
        new_layout.progress.total_length += 1 # TODO line bpd
        new_layout.alternatives = alternatives
        new_layout.elements << LineBox.new(best['layout'].line_layout.elements.dup,
                                           best['diff'])
        new_layout.line_layout.total_demerits = best['dem']
        new_layout.line_layout.progress.part = best['layout'].progress.part + 1
        #$log.debug("New line break: #{new_layout}")
        new_layout
    end

    def handle_new_layouts(new_layouts)
        @paragraph_break_handler.consider_break(Penalty.new(0, 0), new_layouts)
    end
end

class LegalPageBreakHandler < LegalBreakHandler

    public_class_method :new

    def initialize(page_dimensions)
        super(page_dimensions)
    end

    def each_class(layouts, &block)
        layouts.each_block_class(&block)
    end

    def each_layout(layouts, layout_class, &block)
        layouts.each_block_layout(layout_class, &block)
    end

    def get_threshold
        1.0
    end

    def compute_difference(layout)
        @page_dims[layout.progress.part][:bpd] -
                layout.progress.total_length
    end

    def get_progress(layout)
        layout.progress
    end

    def get_demerits(layout)
        layout.total_demerits
    end

    def create_layout(best, alternatives)
        new_layout = Layout.new
        new_layout.total_demerits = best['dem']
        new_layout.previous       = best['layout'].dup
        new_layout.progress.part  = best['layout'].progress.part + 1
        new_layout.alternatives   = alternatives
        new_layout
    end
end

class ParagraphBreakHandler < LegalPageBreakHandler

    def initialize(page_dimensions)
        super(page_dimensions)
    end

    def get_demerits(layout)
        layout.total_demerits + layout.line_layout.total_demerits
    end

    def create_layout(best, alternatives)
        new_layout = LineLayout.new
        new_layout.line_layout    = best['layout'].line_layout.dup
        new_layout.total_demerits = best['dem']
        new_layout.previous       = best['layout']
        new_layout.progress.part  = best['layout'].progress.part + 1
        new_layout.alternatives   = alternatives
        new_layout.is_page        = true
        new_layout
    end
end


class Typographer

    def initialize(page_dimensions)
        @page_dims = page_dimensions
        @page_break_handler = LegalPageBreakHandler.new(page_dimensions)
        paragraph_break_handler = ParagraphBreakHandler.new(page_dimensions)
        @line_break_handler = LegalLineBreakHandler.new(page_dimensions,
                                                        paragraph_break_handler)
    end

    def break(content)
        line_breaker = LineBreaker.new(@line_break_handler)
        page_breaker = PageBreaker.new(@page_break_handler, line_breaker)
        page_breaker.find_breaks(content)
    end
end


Paragraphs = [
    Paragraph.new(%w( In olden times when wishing still helped one, there lived a king
    whose daughters were all beautiful, soooo much beautiful. )),
    Penalty.new(0, 0),
    Glue.new(1, 1, 0, ""),
    Paragraph.new(%w( In olden times when wishing still helped one, there lived a king
    whose daughters were all beautiful, but the youngest was so beautiful that the sun
    itself, which has seen so much, was astonished whenever it shone in her face. )),
#   Penalty.new(0, 0),
#   Glue.new(2, 0, 1, ""),
#   Paragraph.new(%w( And now I am about to start the next paragraph and the goal is to
#   check that the algorithm is working properly, a thing I am not quite sure of. )),
#   Penalty.new(0, 0),
#   Glue.new(2, 0, 1, ""),
#   Paragraph.new(%w( In olden times when wishing still helped one, there lived a king
#   whose daughters were all beautiful, but the youngest was so beautiful that the sun
#   itself, which has seen so much, was astonished whenever it shone in her face. )),
    Glue.new(0, 1000000, 0, ""),
    Penalty.new(0, -Penalty::Infinite)]

Typographer.new([{:ipd => 13000, :bpd => 8},
                {:ipd => 16000, :bpd => 8},
                {:ipd => 13000, :bpd => 8},
                {:ipd => 13000, :bpd => 8},
                {:ipd => 13000, :bpd => 8},
                {:ipd => 13000, :bpd => 8}]).break(Paragraphs)
